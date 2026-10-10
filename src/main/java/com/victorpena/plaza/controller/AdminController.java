package com.victorpena.plaza.controller;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import com.victorpena.plaza.web.LeaseForm;
import jakarta.servlet.http.HttpSession;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
  private final AccessService access;
  private final PropertyRepository properties;
  private final OfficeRepository offices;
  private final LeaseRepository leases;
  private final InvoiceRepository invoices;
  private final PaymentRepository payments;
  private final MaintenanceRequestRepository requests;
  private final PropertyService propertyService;
  private final LeaseService leaseService;
  private final BillingService billing;
  private final InvitationService invites;
  private final MaintenanceRequestService maintenance;
  private final PaymentTransactions transactions;
  private final PaymentService paymentService;
  private final AuditService audit;
  private final NotificationService mail;
  private final DocumentService documents;
  private final StoredDocumentRepository documentRepository;

  public AdminController(
      AccessService access,
      PropertyRepository properties,
      OfficeRepository offices,
      LeaseRepository leases,
      InvoiceRepository invoices,
      PaymentRepository payments,
      MaintenanceRequestRepository requests,
      PropertyService propertyService,
      LeaseService leaseService,
      BillingService billing,
      InvitationService invites,
      MaintenanceRequestService maintenance,
      PaymentTransactions transactions,
      PaymentService paymentService,
      AuditService audit,
      NotificationService mail,
      DocumentService documents,
      StoredDocumentRepository documentRepository) {
    this.access = access;
    this.properties = properties;
    this.offices = offices;
    this.leases = leases;
    this.invoices = invoices;
    this.payments = payments;
    this.requests = requests;
    this.propertyService = propertyService;
    this.leaseService = leaseService;
    this.billing = billing;
    this.invites = invites;
    this.maintenance = maintenance;
    this.transactions = transactions;
    this.paymentService = paymentService;
    this.audit = audit;
    this.mail = mail;
    this.documents = documents;
    this.documentRepository = documentRepository;
  }

  @GetMapping("/dashboard")
  public String dashboard(HttpSession s, Model m) {
    var c = access.company(s);
    var units = offices.findByPropertyCompanyIdOrderByPropertyNameAscOfficeNumberAsc(c.getId());
    var bills = invoices.findByLeaseOfficePropertyCompanyIdOrderByDueDateDesc(c.getId());
    m.addAttribute("company", c);
    m.addAttribute("units", units);
    m.addAttribute("propertyCount", properties.findByCompanyIdOrderByName(c.getId()).size());
    m.addAttribute(
        "balance", bills.stream().map(billing::balance).reduce(BigDecimal.ZERO, BigDecimal::add));
    m.addAttribute(
        "openMaintenance",
        requests.findByOfficePropertyCompanyIdOrderByCreatedAtDesc(c.getId()).stream()
            .filter(r -> r.getStatus() != MaintenanceRequestStatus.COMPLETED)
            .count());
    return "admin-dashboard";
  }

  @GetMapping("/properties")
  public String properties(HttpSession s, Model m) {
    var c = access.company(s);
    m.addAttribute("properties", properties.findByCompanyIdOrderByName(c.getId()));
    m.addAttribute(
        "units", offices.findByPropertyCompanyIdOrderByPropertyNameAscOfficeNumberAsc(c.getId()));
    return "admin-properties";
  }

  @PostMapping("/properties")
  public String saveProperty(
      HttpSession s,
      @RequestParam(required = false) Long id,
      @RequestParam String name,
      @RequestParam String address,
      @RequestParam(defaultValue = "false") boolean active) {
    propertyService.saveProperty(access.company(s), id, name, address, active);
    return "redirect:/admin/properties";
  }

  @PostMapping("/units")
  public String saveUnit(
      HttpSession s,
      @RequestParam(required = false) Long id,
      @RequestParam Long propertyId,
      @RequestParam String officeNumber,
      @RequestParam(required = false) Integer squareFeet,
      @RequestParam(defaultValue = "") String description,
      @RequestParam(defaultValue = "AVAILABLE") OfficeStatus status) {
    propertyService.saveOffice(
        access.company(s).getId(), id, propertyId, officeNumber, squareFeet, description, status);
    return "redirect:/admin/properties";
  }

  @GetMapping({"/leases", "/lease", "/tenants"})
  public String leases(HttpSession s, Model m) {
    m.addAttribute(
        "leases",
        leases.findByOfficePropertyCompanyIdOrderByStartDateDesc(access.company(s).getId()));
    return "admin-leases";
  }

  @GetMapping({"/lease/new", "/leases/new"})
  public String newLease(HttpSession s, @RequestParam(required = false) Long renew, Model m) {
    var c = access.company(s);
    var f = new LeaseForm();
    f.setBillingStartDate(billing.today().withDayOfMonth(1));
    if (renew != null) {
      var l = leaseService.managed(c.getId(), renew);
      f.setOfficeId(l.getOffice().getId());
      f.setTenantName(l.getTenantName());
      f.setTenantEmail(l.getTenantEmail());
      f.setMonthlyRent(l.getMonthlyRent());
      f.setStartDate(l.getEndDate().plusDays(1));
      f.setBillingStartDate(l.getEndDate().plusDays(1).withDayOfMonth(1));
      f.setEndDate(l.getEndDate().plusYears(1));
      f.setDueDay(l.getDueDay());
      f.setGraceDays(l.getGraceDays());
      f.setLateFee(l.getLateFee());
    }
    m.addAttribute("lease", f);
    m.addAttribute(
        "units", offices.findByPropertyCompanyIdOrderByPropertyNameAscOfficeNumberAsc(c.getId()));
    return "lease-form";
  }

  @PostMapping({"/lease/new", "/leases/new"})
  public String createLease(HttpSession s, @ModelAttribute LeaseForm form, RedirectAttributes f) {
    leaseService.create(access.company(s).getId(), form);
    f.addFlashAttribute(
        "successMessage",
        "Lease saved. Any invoices due for generation were created and the invitation was queued.");
    return "redirect:/admin/leases";
  }

  @PostMapping("/leases/{id}/invite")
  public String resend(HttpSession s, @PathVariable Long id) {
    invites.sendInvitation(leaseService.managed(access.company(s).getId(), id));
    return "redirect:/admin/leases";
  }

  @PostMapping("/leases/{id}/terminate")
  public String terminate(HttpSession s, @PathVariable Long id, @RequestParam LocalDate endDate) {
    leaseService.terminate(access.company(s).getId(), id, endDate);
    return "redirect:/admin/leases";
  }

  @GetMapping("/leases/{id}/documents")
  public String documents(HttpSession s, @PathVariable Long id, Model m) {
    var l = leaseService.managed(access.company(s).getId(), id);
    m.addAttribute("lease", l);
    m.addAttribute("documents", documentRepository.findByLeaseIdOrderByCreatedAtDesc(id));
    return "lease-documents";
  }

  @PostMapping("/leases/{id}/documents")
  public String upload(
      HttpSession s,
      @PathVariable Long id,
      @RequestParam org.springframework.web.multipart.MultipartFile file)
      throws java.io.IOException {
    documents.upload(access.company(s).getId(), id, file);
    return "redirect:/admin/leases/" + id + "/documents";
  }

  @GetMapping("/invoices")
  public String invoices(HttpSession s, Model m) {
    var c = access.company(s);
    m.addAttribute(
        "invoices", invoices.findByLeaseOfficePropertyCompanyIdOrderByDueDateDesc(c.getId()));
    m.addAttribute("leases", leases.findByOfficePropertyCompanyIdOrderByStartDateDesc(c.getId()));
    m.addAttribute("billing", billing);
    m.addAttribute("formKey", UUID.randomUUID().toString());
    return "admin-invoices";
  }

  @PostMapping("/invoices/generate")
  public String generate(HttpSession s) {
    for (var l :
        leases.findByOfficePropertyCompanyIdOrderByStartDateDesc(access.company(s).getId()))
      billing.generate(l.getId());
    return "redirect:/admin/invoices";
  }

  @PostMapping("/invoices/charge")
  @Transactional
  public String charge(
      HttpSession s,
      @RequestParam Long leaseId,
      @RequestParam String description,
      @RequestParam BigDecimal amount,
      @RequestParam LocalDate dueDate) {
    var c = access.company(s);
    var l = leaseService.managed(c.getId(), leaseId);
    var i = new Invoice();
    i.setLease(l);
    i.setKind("OTHER");
    i.setDescription(Checks.text(description, "Description", 255));
    i.setAmount(Checks.money(amount, "Charge", false));
    i.setDueDate(dueDate);
    i.setCreatedAt(LocalDateTime.now());
    invoices.saveAndFlush(i);
    audit.record(
        c,
        access.user().getEmail(),
        "CHARGE_CREATED",
        "Invoice " + i.getId() + " $" + i.getAmount());
    mail.send(
        l.getTenantEmail(),
        "New charge",
        description + " $" + amount + " due " + dueDate + ". " + mail.url("/tenant/invoices"));
    return "redirect:/admin/invoices";
  }

  @PostMapping("/invoices/{id}/void")
  @Transactional
  public String voidInvoice(HttpSession s, @PathVariable Long id, @RequestParam String reason) {
    var c = access.company(s);
    var i = invoices.lockById(id).orElseThrow(AccessService::missing);
    access.sameCompany(c.getId(), i.getLease().getOffice().getProperty().getCompany());
    if (billing.pending(i) || billing.paid(i).signum() > 0)
      throw new IllegalArgumentException("Resolve payments before voiding this invoice.");
    i.setStatus(InvoiceStatus.VOID);
    audit.record(
        c,
        access.user().getEmail(),
        "INVOICE_VOIDED",
        "Invoice " + id + ": " + Checks.text(reason, "Reason", 500));
    return "redirect:/admin/invoices";
  }

  @PostMapping("/invoices/{id}/offline")
  public String offline(
      HttpSession s,
      @PathVariable Long id,
      @RequestParam BigDecimal amount,
      @RequestParam String method,
      @RequestParam String reference,
      @RequestParam String note,
      @RequestParam String key) {
    transactions.offline(access.company(s).getId(), id, amount, method, reference, note, key);
    return "redirect:/admin/invoices";
  }

  @GetMapping("/payments")
  public String payments(HttpSession s, Model m) {
    m.addAttribute(
        "payments",
        payments.findByOfficePropertyCompanyIdOrderByCreatedAtDesc(access.company(s).getId()));
    return "admin-payments";
  }

  @PostMapping("/payments/{id}/reconcile")
  public String reconcile(HttpSession s, @PathVariable Long id) {
    paymentService.reconcile(access.company(s).getId(), id);
    return "redirect:/admin/payments";
  }

  @PostMapping("/payments/{id}/cancel")
  public String cancelCheckout(HttpSession s, @PathVariable Long id) {
    paymentService.cancelCheckout(access.company(s).getId(), id);
    return "redirect:/admin/payments";
  }

  @GetMapping("/maintenance")
  public String maintenance(HttpSession s, Model m) {
    m.addAttribute(
        "requests",
        requests.findByOfficePropertyCompanyIdOrderByCreatedAtDesc(access.company(s).getId()));
    return "admin-maintenance-requests";
  }

  @PostMapping("/maintenance/{id}/status")
  public String updateMaintenance(
      HttpSession s,
      @PathVariable Long id,
      @RequestParam MaintenanceRequestStatus status,
      @RequestParam(defaultValue = "") String note) {
    maintenance.update(access.company(s).getId(), id, status, note);
    return "redirect:/admin/maintenance";
  }
}
