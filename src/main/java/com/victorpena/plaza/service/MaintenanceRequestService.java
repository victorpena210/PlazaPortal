package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceRequestService {
  private final MaintenanceRequestRepository requests;
  private final LeaseRepository leases;
  private final AccessService access;
  private final NotificationService mail;
  private final AuditService audit;
  private final BillingService billing;

  public MaintenanceRequestService(
      MaintenanceRequestRepository requests,
      LeaseRepository leases,
      AccessService access,
      NotificationService mail,
      AuditService audit,
      BillingService billing) {
    this.requests = requests;
    this.leases = leases;
    this.access = access;
    this.mail = mail;
    this.audit = audit;
    this.billing = billing;
  }

  @Transactional
  public void create(Long leaseId, String title, String description, PriorityLevel priority) {
    var l = leases.findById(leaseId).orElseThrow(AccessService::missing);
    access.tenant(l);
    if (!l.isActive()
        || billing.today().isBefore(l.getStartDate())
        || (l.getEndDate() != null && billing.today().isAfter(l.getEndDate())))
      throw new IllegalArgumentException("Select a current lease.");
    var r = new MaintenanceRequest();
    r.setLease(l);
    r.setOffice(l.getOffice());
    r.setUser(access.user());
    r.setTitle(Checks.text(title, "Title", 150));
    r.setDescription(Checks.text(description, "Description", 10000));
    r.setPriority(priority);
    requests.saveAndFlush(r);
    var c = l.getOffice().getProperty().getCompany();
    mail.owners(
        c,
        "Maintenance request · " + l.getOffice().getOfficeNumber(),
        r.getTitle() + ". Review request " + r.getId() + ": " + mail.url("/admin/maintenance"));
    audit.record(c, access.user().getEmail(), "MAINTENANCE_CREATED", "Request " + r.getId());
  }

  @Transactional
  public void update(Long companyId, Long id, MaintenanceRequestStatus status, String note) {
    var r = requests.findById(id).orElseThrow(AccessService::missing);
    var c = r.getOffice().getProperty().getCompany();
    access.sameCompany(companyId, c);
    r.setStatus(status);
    r.setAdminNote(note == null || note.isBlank() ? null : Checks.text(note, "Update", 5000));
    r.setCompletedAt(status == MaintenanceRequestStatus.COMPLETED ? LocalDateTime.now() : null);
    mail.send(
        r.getUser().getEmail(),
        "Maintenance update",
        r.getTitle()
            + ": "
            + status
            + ". "
            + (r.getAdminNote() == null ? "" : r.getAdminNote())
            + " View: "
            + mail.url("/tenant/dashboard"));
    audit.record(
        c, access.user().getEmail(), "MAINTENANCE_UPDATED", "Request " + id + " → " + status);
  }

  @Transactional
  public void comment(Long id, String comment) {
    var r = requests.findById(id).orElseThrow(AccessService::missing);
    if (!r.getUser().getId().equals(access.user().getId())) throw AccessService.missing();
    r.setTenantComment(Checks.text(comment, "Comment", 5000));
    mail.owners(
        r.getOffice().getProperty().getCompany(),
        "Tenant maintenance comment",
        "Request " + id + " has a tenant update. " + mail.url("/admin/maintenance"));
  }
}
