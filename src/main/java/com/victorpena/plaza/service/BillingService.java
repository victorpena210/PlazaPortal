package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {
  private final LeaseRepository leases;
  private final InvoiceRepository invoices;
  private final PaymentRepository payments;
  private final NotificationService notifications;
  private final Clock clock;

  public BillingService(
      LeaseRepository leases,
      InvoiceRepository invoices,
      PaymentRepository payments,
      NotificationService notifications,
      Clock clock) {
    this.leases = leases;
    this.invoices = invoices;
    this.payments = payments;
    this.notifications = notifications;
    this.clock = clock;
  }

  public LocalDate today() {
    return LocalDate.now(clock);
  }

  public BigDecimal paid(Invoice i) {
    return payments.findByInvoiceIdOrderById(i.getId()).stream()
        .filter(p -> p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.REFUNDED)
        .map(p -> p.getTotalAmount().subtract(p.getRefundedAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal balance(Invoice i) {
    return i.getStatus() == InvoiceStatus.VOID
        ? BigDecimal.ZERO
        : i.getTotal().subtract(paid(i)).max(BigDecimal.ZERO);
  }

  public boolean pending(Invoice i) {
    return payments.findByInvoiceIdOrderById(i.getId()).stream()
        .anyMatch(
            p ->
                p.getStatus() == PaymentStatus.PENDING
                    || p.getStatus() == PaymentStatus.PROCESSING
                    || p.getStatus() == PaymentStatus.REVIEW_REQUIRED);
  }

  public void refresh(Invoice i) {
    if (i.getStatus() == InvoiceStatus.VOID) return;
    var paid = paid(i);
    i.setStatus(
        paid.compareTo(i.getTotal()) >= 0
            ? InvoiceStatus.PAID
            : today().isAfter(i.getDueDate())
                ? InvoiceStatus.OVERDUE
                : paid.signum() > 0 ? InvoiceStatus.PARTIALLY_PAID : InvoiceStatus.PENDING);
    invoices.save(i);
  }

  @Transactional
  public void generate(Long leaseId) {
    var l = leases.lockById(leaseId).orElseThrow();
    if (!l.isActive()) return;
    var last = YearMonth.from(today().plusMonths(1));
    if (l.getEndDate() != null && YearMonth.from(l.getEndDate()).isBefore(last))
      last = YearMonth.from(l.getEndDate());
    var leaseFirst = YearMonth.from(l.getStartDate());
    var first =
        l.getBillingStartDate() == null ? leaseFirst : YearMonth.from(l.getBillingStartDate());
    if (first.isBefore(leaseFirst)) first = leaseFirst;
    for (var month = first; !month.isAfter(last); month = month.plusMonths(1)) {
      var period = month.atDay(1);
      if (invoices.existsByLeaseIdAndBillingPeriodAndKind(l.getId(), period, "RENT")) continue;
      var i = new Invoice();
      i.setLease(l);
      i.setBillingPeriod(period);
      i.setKind("RENT");
      i.setDescription("Rent · " + month);
      var amount = l.getMonthlyRent();
      if (month.equals(leaseFirst) && l.isProrateFirstMonth())
        amount =
            amount
                .multiply(
                    BigDecimal.valueOf(
                        month.lengthOfMonth() - l.getStartDate().getDayOfMonth() + 1))
                .divide(BigDecimal.valueOf(month.lengthOfMonth()), 2, RoundingMode.HALF_UP);
      i.setAmount(amount);
      var due = month.atDay(Math.min(l.getDueDay(), month.lengthOfMonth()));
      if (due.isBefore(l.getStartDate())) due = l.getStartDate();
      i.setDueDate(due);
      i.setCreatedAt(LocalDateTime.now(clock));
      invoices.saveAndFlush(i);
      notifications.send(
          l.getTenantEmail(),
          "New invoice from " + l.getOffice().getProperty().getCompany().getName(),
          i.getDescription()
              + ": $"
              + i.getAmount()
              + ". Due "
              + due
              + ". View your account: "
              + notifications.url("/tenant/invoices"));
    }
  }

  @Transactional
  public void updateOverdue(Long invoiceId) {
    var i = invoices.lockById(invoiceId).orElseThrow();
    if (i.getStatus() == InvoiceStatus.VOID || balance(i).signum() == 0) return;
    var l = i.getLease();
    if (i.getKind().equals("RENT")
        && today().isAfter(i.getDueDate().plusDays(l.getGraceDays()))
        && i.getLateFeeAmount().signum() == 0
        && l.getLateFee().signum() > 0
        && !pending(i)) {
      i.setLateFeeAmount(l.getLateFee());
      notifications.send(
          l.getTenantEmail(),
          "Invoice overdue",
          "Invoice "
              + i.getId()
              + " is overdue. Your lease's configured late fee has been added. "
              + notifications.url("/tenant/invoices"));
    }
    refresh(i);
  }
}
