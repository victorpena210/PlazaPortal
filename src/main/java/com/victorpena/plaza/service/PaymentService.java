package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.time.*;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
  private final PaymentTransactions transactions;
  private final StripeGateway stripe;
  private final PaymentRepository payments;
  private final AccessService access;

  public PaymentService(
      PaymentTransactions transactions,
      StripeGateway stripe,
      PaymentRepository payments,
      AccessService access) {
    this.transactions = transactions;
    this.stripe = stripe;
    this.payments = payments;
    this.access = access;
  }

  public String checkout(Long invoiceId) {
    var p = transactions.reserve(invoiceId);
    if (!stripe
        .account(p.getOffice().getProperty().getCompany().getId())
        .equals(p.getStripeAccountId()))
      throw new IllegalArgumentException("Payment account settings changed. Contact your manager.");
    if (p.getStripeSessionId() != null) {
      var s = stripe.retrieve(p.getStripeSessionId(), p.getStripeAccountId());
      transactions.apply(null, s, p.getStripeAccountId(), "reconcile");
      if ("open".equals(s.getStatus())) return s.getUrl();
      if ("expired".equals(s.getStatus())) p = transactions.reserve(invoiceId);
      else
        throw new IllegalArgumentException(
            "Payment is processing or completed. Refresh your invoices.");
    }
    if (p.getCreatedAt().isBefore(LocalDateTime.now().minusHours(23)))
      throw new IllegalArgumentException(
          "This pending attempt needs a manager's review before retrying.");
    var s = stripe.create(p);
    transactions.attach(p.getId(), s);
    return s.getUrl();
  }

  public void cancelCheckout(Long companyId, Long paymentId) {
    var p = payments.findById(paymentId).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, p.getOffice().getProperty().getCompany());
    if (p.getStripeSessionId() == null)
      throw new IllegalArgumentException("This attempt must be recovered and verified first.");
    var s = stripe.retrieve(p.getStripeSessionId(), p.getStripeAccountId());
    if ("open".equals(s.getStatus()))
      s = stripe.expire(p.getStripeSessionId(), p.getStripeAccountId());
    transactions.apply(null, s, p.getStripeAccountId(), "reconcile");
    if (!"expired".equals(s.getStatus()))
      throw new IllegalArgumentException(
          "This checkout has already been submitted. Wait for its confirmed result.");
  }

  public void reconcile(Long companyId, Long paymentId) {
    var p = payments.findById(paymentId).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, p.getOffice().getProperty().getCompany());
    if (p.getStripeSessionId() == null) {
      if (!"STRIPE".equals(p.getPaymentMethod())
          || p.getCreatedAt().isBefore(LocalDateTime.now().minusHours(23)))
        throw new IllegalArgumentException(
            "Review this attempt in Stripe; its safe automatic retry window has ended.");
      var recovered = stripe.create(p);
      transactions.attach(p.getId(), recovered);
      p = payments.findById(paymentId).orElseThrow();
    }
    var s = stripe.retrieve(p.getStripeSessionId(), p.getStripeAccountId());
    transactions.apply(null, s, p.getStripeAccountId(), "reconcile");
    if ("paid".equals(s.getPaymentStatus())) {
      var charge = stripe.latestCharge(s.getPaymentIntent(), p.getStripeAccountId());
      if (charge != null && charge.getAmountRefunded() != null && charge.getAmountRefunded() > 0)
        transactions.refund(
            "refund-sync-" + charge.getId() + "-" + charge.getAmountRefunded(),
            charge,
            p.getStripeAccountId());
    }
  }
}
