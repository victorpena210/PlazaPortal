package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.web.LeaseForm;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaseService {
  private final LeaseRepository leases;
  private final OfficeRepository offices;
  private final InvoiceRepository invoices;
  private final AccessService access;
  private final BillingService billing;
  private final InvitationService invites;
  private final AuditService audit;

  public LeaseService(
      LeaseRepository leases,
      OfficeRepository offices,
      InvoiceRepository invoices,
      AccessService access,
      BillingService billing,
      InvitationService invites,
      AuditService audit) {
    this.leases = leases;
    this.offices = offices;
    this.invoices = invoices;
    this.access = access;
    this.billing = billing;
    this.invites = invites;
    this.audit = audit;
  }

  public Lease managed(Long companyId, Long id) {
    var l = leases.findById(id).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, l.getOffice().getProperty().getCompany());
    return l;
  }

  @Transactional
  public Lease create(Long companyId, LeaseForm f) {
    var office = offices.lockById(f.getOfficeId()).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, office.getProperty().getCompany());
    if (!office.getProperty().isActive() || office.getStatus() == OfficeStatus.MAINTENANCE)
      throw new IllegalArgumentException("This unit is unavailable for leasing.");
    if (f.getStartDate() == null
        || f.getEndDate() == null
        || f.getEndDate().isBefore(f.getStartDate())
        || f.getStartDate().isBefore(billing.today().minusYears(10)))
      throw new IllegalArgumentException(
          "Enter valid lease dates (start no more than 10 years ago).");
    if (f.getDueDay() < 1 || f.getDueDay() > 28 || f.getGraceDays() < 0 || f.getGraceDays() > 90)
      throw new IllegalArgumentException("Due day must be 1–28 and grace days 0–90.");
    if (!leases.overlapping(office.getId(), f.getStartDate(), f.getEndDate()).isEmpty())
      throw new IllegalArgumentException("This unit already has an overlapping lease.");
    var l = new Lease();
    l.setOffice(office);
    l.setTenantName(Checks.text(f.getTenantName(), "Tenant name", 255));
    l.setTenantEmail(Checks.email(f.getTenantEmail()));
    l.setMonthlyRent(Checks.money(f.getMonthlyRent(), "Rent", false));
    l.setStartDate(f.getStartDate());
    var billingStart =
        f.getBillingStartDate() == null
            ? billing.today().withDayOfMonth(1)
            : f.getBillingStartDate().withDayOfMonth(1);
    if (billingStart.isBefore(f.getStartDate().withDayOfMonth(1)))
      billingStart = f.getStartDate().withDayOfMonth(1);
    l.setBillingStartDate(billingStart);
    l.setEndDate(f.getEndDate());
    l.setDueDay(f.getDueDay());
    l.setProrateFirstMonth(f.isProrateFirstMonth());
    l.setGraceDays(f.getGraceDays());
    l.setLateFee(Checks.money(f.getLateFee(), "Late fee", true));
    leases.saveAndFlush(l);
    billing.generate(l.getId());
    invites.sendInvitation(l);
    if (!billing.today().isBefore(l.getStartDate()) && !billing.today().isAfter(l.getEndDate()))
      office.setStatus(OfficeStatus.OCCUPIED);
    audit.record(
        office.getProperty().getCompany(),
        access.user().getEmail(),
        "LEASE_CREATED",
        "Lease " + l.getId());
    return l;
  }

  @Transactional
  public void terminate(Long companyId, Long id, LocalDate end) {
    var l = leases.lockById(id).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, l.getOffice().getProperty().getCompany());
    if (end == null || end.isBefore(l.getStartDate()) || end.isAfter(l.getEndDate()))
      throw new IllegalArgumentException("Termination must fall within the current lease dates.");
    for (var i : invoices.findByLeaseIdOrderByDueDate(id)) {
      if (i.getKind().equals("RENT")
          && i.getBillingPeriod() != null
          && i.getBillingPeriod().isAfter(YearMonth.from(end).atDay(1))) {
        if (billing.pending(i) || billing.paid(i).signum() > 0)
          throw new IllegalArgumentException("Resolve future payments before ending this lease.");
        i.setStatus(InvoiceStatus.VOID);
      }
    }
    l.setEndDate(end);
    if (end.isBefore(billing.today())) l.setActive(false);
    audit.record(
        l.getOffice().getProperty().getCompany(),
        access.user().getEmail(),
        "LEASE_TERMINATED",
        "Lease " + id + " end " + end);
  }
}
