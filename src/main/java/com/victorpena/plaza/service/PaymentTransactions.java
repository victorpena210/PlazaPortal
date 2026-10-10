package com.victorpena.plaza.service;

import com.stripe.model.Charge;
import com.stripe.model.checkout.Session;
import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentTransactions {
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
  private final InvoiceRepository invoices;
  private final PaymentRepository payments;
  private final ProcessedWebhookRepository events;
  private final AccessService access;
  private final BillingService billing;
  private final StripeGateway stripe;
  private final AuditService audit;
  private final NotificationService mail;

  public PaymentTransactions(
      InvoiceRepository invoices,
      PaymentRepository payments,
      ProcessedWebhookRepository events,
      AccessService access,
      BillingService billing,
      StripeGateway stripe,
      AuditService audit,
      NotificationService mail) {
    this.invoices = invoices;
    this.payments = payments;
    this.events = events;
    this.access = access;
    this.billing = billing;
    this.stripe = stripe;
    this.audit = audit;
    this.mail = mail;
  }

  @Transactional
  public Payment reserve(Long invoiceId) {
    var i = invoices.lockById(invoiceId).orElseThrow(AccessService::missing);
    access.tenant(i.getLease());
    if (i.getStatus() == InvoiceStatus.VOID || billing.balance(i).signum() == 0)
      throw new IllegalArgumentException("This invoice has no outstanding balance.");
    for (var p : payments.findByInvoiceIdOrderById(invoiceId)) {
      if (p.getStatus() == PaymentStatus.REVIEW_REQUIRED
          || p.getStatus() == PaymentStatus.PROCESSING)
        throw new IllegalArgumentException(
            "A payment is processing or needs review. Contact your manager before paying again.");
      if (p.getStatus() == PaymentStatus.PENDING) return p;
    }
    var p = base(i, billing.balance(i));
    p.setAttemptKey("plaza-" + UUID.randomUUID());
    p.setStripeAccountId(
        stripe.account(i.getLease().getOffice().getProperty().getCompany().getId()));
    p.setPaymentMethod("STRIPE");
    p.setCheckoutExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(23).withNano(0));
    p.setStatus(PaymentStatus.PENDING);
    return payments.saveAndFlush(p);
  }

  private Payment base(Invoice i, BigDecimal amount) {
    var p = new Payment();
    p.setInvoice(i);
    p.setLease(i.getLease());
    p.setUser(i.getLease().getPortalAccess());
    p.setOffice(i.getLease().getOffice());
    p.setAmount(amount);
    p.setTotalAmount(amount);
    p.setPaymentMonth(java.time.YearMonth.from(i.getDueDate()).toString());
    return p;
  }

  @Transactional
  public Payment attach(Long paymentId, Session s) {
    var p = payments.findById(paymentId).orElseThrow();
    invoices.lockById(p.getInvoice().getId()).orElseThrow();
    entityManager.refresh(p);
    if (p.getStripeSessionId() != null && !p.getStripeSessionId().equals(s.getId()))
      throw new IllegalStateException("Checkout session conflict.");
    validate(p, s, p.getStripeAccountId());
    p.setStripeSessionId(s.getId());
    p.setCheckoutUrl(s.getUrl());
    p.setStripePaymentIntentId(s.getPaymentIntent());
    return payments.save(p);
  }

  private void validate(Payment p, Session s, String account) {
    var i = p.getInvoice();
    var metadata = s.getMetadata();
    if (!Objects.equals(account, p.getStripeAccountId())
        || !Objects.equals(s.getLivemode(), stripe.live())
        || !"usd".equals(s.getCurrency())
        || s.getAmountTotal() == null
        || s.getAmountTotal() != p.getTotalAmount().movePointRight(2).longValueExact()
        || metadata == null
        || !p.getId().toString().equals(metadata.get("payment_id"))
        || !i.getId().toString().equals(metadata.get("invoice_id"))
        || !i.getLease()
            .getOffice()
            .getProperty()
            .getCompany()
            .getId()
            .toString()
            .equals(metadata.get("company_id")))
      throw new IllegalArgumentException("Payment verification mismatch.");
  }

  @Transactional
  public void apply(String eventId, Session s, String account, String type) {
    var payment = payments.findByStripeSessionId(s.getId());
    if (payment.isEmpty() && s.getMetadata() != null && s.getMetadata().get("payment_id") != null) {
      try {
        payment = payments.findById(Long.valueOf(s.getMetadata().get("payment_id")));
      } catch (NumberFormatException ignored) {
      }
    }
    if (payment.isEmpty()) return;
    var p = payment.get();
    var i = invoices.lockById(p.getInvoice().getId()).orElseThrow();
    if (eventId != null && events.existsById(eventId)) return;
    entityManager.refresh(p);
    validate(p, s, account);
    if (p.getStripeSessionId() != null && !p.getStripeSessionId().equals(s.getId()))
      throw new IllegalArgumentException("Unexpected checkout session.");
    p.setStripeSessionId(s.getId());
    p.setStripePaymentIntentId(s.getPaymentIntent());
    p.setCheckoutUrl(s.getUrl());
    boolean already =
        p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.REFUNDED;
    if ("paid".equals(s.getPaymentStatus()) && !already) {
      p.setStatus(PaymentStatus.PAID);
      p.setPaidAt(LocalDateTime.now());
      p.setTransactionReference(s.getPaymentIntent());
      payments.saveAndFlush(p);
      billing.refresh(i);
      var c = i.getLease().getOffice().getProperty().getCompany();
      audit.record(
          c, "stripe", "PAYMENT_CONFIRMED", "Invoice " + i.getId() + ", payment " + p.getId());
      mail.send(
          i.getLease().getTenantEmail(),
          "Payment receipt",
          "Payment of $"
              + p.getTotalAmount()
              + " received for invoice "
              + i.getId()
              + ". Receipt: "
              + mail.url("/receipts/" + p.getId()));
    } else if (!already) {
      if ("expired".equals(s.getStatus())) p.setStatus(PaymentStatus.CANCELED);
      else if ("checkout.session.async_payment_failed".equals(type)) {
        p.setStatus(PaymentStatus.FAILED);
        mail.send(
            i.getLease().getTenantEmail(),
            "Payment unsuccessful",
            "Your payment did not complete. View invoice "
                + i.getId()
                + ": "
                + mail.url("/tenant/invoices"));
      } else if ("complete".equals(s.getStatus())) p.setStatus(PaymentStatus.PROCESSING);
    }
    if (eventId != null) events.save(new ProcessedWebhook(eventId));
  }

  @Transactional
  public void refund(String eventId, Charge charge, String account) {
    var optional = payments.findByStripePaymentIntentId(charge.getPaymentIntent());
    if (optional.isEmpty()) return;
    var p = optional.get();
    var i = invoices.lockById(p.getInvoice().getId()).orElseThrow();
    if (events.existsById(eventId)) return;
    entityManager.refresh(p);
    if (!Objects.equals(p.getStripeAccountId(), account)
        || !Objects.equals(charge.getLivemode(), stripe.live())
        || !"usd".equals(charge.getCurrency())
        || charge.getAmount() == null
        || charge.getAmount() != p.getTotalAmount().movePointRight(2).longValueExact())
      throw new IllegalArgumentException("Refund verification mismatch.");
    BigDecimal refunded = BigDecimal.valueOf(charge.getAmountRefunded(), 2);
    if (refunded.compareTo(p.getTotalAmount()) > 0)
      throw new IllegalArgumentException("Unexpected refund amount.");
    p.setRefundedAmount(refunded);
    p.setStatus(
        refunded.compareTo(p.getTotalAmount()) == 0 ? PaymentStatus.REFUNDED : PaymentStatus.PAID);
    if (p.getPaidAt() == null) p.setPaidAt(LocalDateTime.now());
    payments.saveAndFlush(p);
    billing.refresh(i);
    events.save(new ProcessedWebhook(eventId));
    audit.record(
        i.getLease().getOffice().getProperty().getCompany(),
        "stripe",
        "PAYMENT_REFUNDED",
        "Payment " + p.getId() + ", refunded $" + refunded);
    mail.send(
        i.getLease().getTenantEmail(),
        "Payment refund recorded",
        "Refund of $"
            + refunded
            + " recorded for invoice "
            + i.getId()
            + ". Contact your manager about any remaining balance.");
  }

  @Transactional
  public void offline(
      Long companyId,
      Long invoiceId,
      BigDecimal amount,
      String method,
      String reference,
      String note,
      String key) {
    var i = invoices.lockById(invoiceId).orElseThrow(AccessService::missing);
    var c = i.getLease().getOffice().getProperty().getCompany();
    access.sameCompany(companyId, c);
    if (key == null || !key.matches("[a-fA-F0-9-]{36}"))
      throw new IllegalArgumentException("Reload the payment form.");
    if (payments.findByAttemptKey("offline-" + invoiceId + "-" + key).isPresent()) return;
    amount = Checks.money(amount, "Payment", false);
    if (i.getStatus() == InvoiceStatus.VOID || amount.compareTo(billing.balance(i)) > 0)
      throw new IllegalArgumentException("Payment exceeds the remaining balance.");
    if (billing.pending(i))
      throw new IllegalArgumentException(
          "Resolve the active online payment before recording an offline payment.");
    if (!Set.of("CHECK", "CASH", "BANK_TRANSFER", "OTHER").contains(method))
      throw new IllegalArgumentException("Choose a payment method.");
    var p = base(i, amount);
    p.setAttemptKey("offline-" + invoiceId + "-" + key);
    p.setPaymentMethod(method);
    p.setTransactionReference(Checks.text(reference, "Reference", 255));
    p.setNote(Checks.text(note, "Record note", 500));
    p.setStatus(PaymentStatus.PAID);
    p.setPaidAt(LocalDateTime.now());
    payments.saveAndFlush(p);
    billing.refresh(i);
    audit.record(
        c,
        access.user().getEmail(),
        "OFFLINE_PAYMENT_RECORDED",
        "Invoice " + i.getId() + ", payment " + p.getId() + ", $" + amount);
    mail.send(
        i.getLease().getTenantEmail(),
        "Payment recorded",
        "Your manager recorded $"
            + amount
            + " for invoice "
            + i.getId()
            + ". Receipt: "
            + mail.url("/receipts/" + p.getId()));
  }
}
