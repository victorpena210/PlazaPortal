package com.victorpena.plaza.controller;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tenant")
public class TenantController {
  private final AccessService access;
  private final LeaseRepository leases;
  private final InvoiceRepository invoices;
  private final PaymentRepository payments;
  private final MaintenanceRequestRepository requests;
  private final MaintenanceRequestService maintenance;
  private final BillingService billing;
  private final StripeGateway stripe;
  private final StoredDocumentRepository documents;

  public TenantController(
      AccessService access,
      LeaseRepository leases,
      InvoiceRepository invoices,
      PaymentRepository payments,
      MaintenanceRequestRepository requests,
      MaintenanceRequestService maintenance,
      BillingService billing,
      StripeGateway stripe,
      StoredDocumentRepository documents) {
    this.access = access;
    this.leases = leases;
    this.invoices = invoices;
    this.payments = payments;
    this.requests = requests;
    this.maintenance = maintenance;
    this.billing = billing;
    this.stripe = stripe;
    this.documents = documents;
  }

  @GetMapping("/dashboard")
  public String dashboard(Model m) {
    var id = access.user().getId();
    m.addAttribute("leases", leases.findByPortalAccessIdOrderByStartDateDesc(id));
    m.addAttribute("requests", requests.findByUserIdOrderByCreatedAtDesc(id));
    return "tenant-dashboard";
  }

  @GetMapping("/maintenance/new")
  public String maintenance(Model m) {
    m.addAttribute(
        "leases",
        leases.findByPortalAccessIdOrderByStartDateDesc(access.user().getId()).stream()
            .filter(
                l ->
                    l.isActive()
                        && !billing.today().isBefore(l.getStartDate())
                        && (l.getEndDate() == null || !billing.today().isAfter(l.getEndDate())))
            .toList());
    return "tenant-maintenance-form";
  }

  @PostMapping("/maintenance")
  public String create(
      @RequestParam Long leaseId,
      @RequestParam String title,
      @RequestParam String description,
      @RequestParam(defaultValue = "MEDIUM") PriorityLevel priority) {
    maintenance.create(leaseId, title, description, priority);
    return "redirect:/tenant/dashboard";
  }

  @PostMapping("/maintenance/{id}/comment")
  public String comment(@PathVariable Long id, @RequestParam String comment) {
    maintenance.comment(id, comment);
    return "redirect:/tenant/dashboard";
  }

  @GetMapping("/invoices")
  public String invoices(Model m) {
    m.addAttribute(
        "invoices", invoices.findByLeasePortalAccessIdOrderByDueDateDesc(access.user().getId()));
    m.addAttribute("billing", billing);
    m.addAttribute("stripe", stripe);
    return "tenant-invoices";
  }

  @GetMapping("/payments/new")
  public String newPayment() {
    return "redirect:/tenant/invoices";
  }

  @GetMapping("/payments")
  public String payments(Model m) {
    m.addAttribute("payments", payments.findByUserIdOrderByCreatedAtDesc(access.user().getId()));
    return "tenant-payments";
  }

  @GetMapping("/lease")
  public String leases(Model m) {
    m.addAttribute(
        "leases", leases.findByPortalAccessIdOrderByStartDateDesc(access.user().getId()));
    m.addAttribute("documents", documents);
    return "tenant-lease";
  }

  @GetMapping("/pay/{id}")
  public String oldCheckout(@PathVariable Long id) {
    var i = invoices.findById(id).orElseThrow(AccessService::missing);
    access.tenant(i.getLease());
    return "redirect:/tenant/invoices";
  }
}
