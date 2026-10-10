package com.victorpena.plaza;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stripe.model.Charge;
import com.stripe.model.checkout.Session;
import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import com.victorpena.plaza.web.LeaseForm;
import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(
    properties = {
      "stripe.webhook-secret=whsec_local_test_secret",
      "app.base-url=https://portal.example.test"
    })
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PortalWorkflowTest {
  @Autowired MockMvc mvc;
  @Autowired UserService users;
  @Autowired CompanyService companies;
  @Autowired CompanyRepository companyRepository;
  @Autowired CompanyMembershipRepository memberships;
  @Autowired PropertyService properties;
  @Autowired PropertyRepository propertyRepository;
  @Autowired OfficeRepository offices;
  @Autowired LeaseRepository leases;
  @Autowired LeaseService leasing;
  @Autowired InvoiceRepository invoices;
  @Autowired BillingService billing;
  @Autowired PaymentRepository payments;
  @Autowired PaymentTransactions transactions;
  @Autowired PaymentService paymentService;
  @Autowired InvitationService invites;
  @Autowired TenantInvitationRepository invitationRepository;
  @Autowired MailOutboxRepository outbox;
  @Autowired MaintenanceRequestRepository requests;
  @Autowired MaintenanceRequestService maintenance;
  @Autowired AccessService access;
  @Autowired PasswordResetService resets;
  @Autowired AuditEntryRepository audits;
  @Autowired DocumentService documentService;
  @Autowired StoredDocumentRepository documents;
  @Autowired MaintenancePhotoService photoService;
  @Autowired MaintenancePhotoRepository photos;
  @MockitoBean StripeGateway stripe;
  @MockitoBean Clock clock;
  User owner, otherOwner, tenant, otherTenant;
  Company company, otherCompany;
  Property property, otherProperty;
  Office office, otherOffice;
  Lease lease, otherLease;
  Invoice invoice, otherInvoice;

  @BeforeEach
  void setup() {
    when(clock.instant()).thenReturn(Instant.parse("2026-10-10T12:00:00Z"));
    when(clock.getZone()).thenReturn(ZoneId.of("America/Chicago"));
    owner =
        users.registerUser(
            "Owner", "One", "owner1@example.test", "a-long-test-password", Role.ADMIN);
    otherOwner =
        users.registerUser(
            "Owner", "Two", "owner2@example.test", "a-long-test-password", Role.ADMIN);
    tenant =
        users.registerUser(
            "Tenant", "One", "tenant1@example.test", "a-long-test-password", Role.TENANT);
    otherTenant =
        users.registerUser(
            "Tenant", "Two", "tenant2@example.test", "a-long-test-password", Role.TENANT);
    company = companies.create(owner, "Company One");
    otherCompany = companies.create(otherOwner, "Company Two");
    login(owner);
    property = properties.saveProperty(company, null, "Building One", "1 Main Street", true);
    office =
        properties.saveOffice(
            company.getId(), null, property.getId(), "101", 1000, "", OfficeStatus.AVAILABLE);
    login(otherOwner);
    otherProperty =
        properties.saveProperty(otherCompany, null, "Building Two", "2 Main Street", true);
    otherOffice =
        properties.saveOffice(
            otherCompany.getId(),
            null,
            otherProperty.getId(),
            "101",
            1000,
            "",
            OfficeStatus.AVAILABLE);
    lease = seedLease(office, tenant);
    otherLease = seedLease(otherOffice, otherTenant);
    invoice = seedInvoice(lease);
    otherInvoice = seedInvoice(otherLease);
    login(owner);
    when(stripe.account(company.getId())).thenReturn("acct_one");
    when(stripe.enabled(company.getId())).thenReturn(true);
    when(stripe.live()).thenReturn(false);
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  void login(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                u.getEmail(), "", List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))));
  }

  MockHttpSession session(Company c) {
    var s = new MockHttpSession();
    s.setAttribute("companyId", c.getId());
    return s;
  }

  Lease seedLease(Office office, User user) {
    var l = new Lease();
    l.setOffice(office);
    l.setTenantName(user.getFirstName());
    l.setTenantEmail(user.getEmail());
    l.setPortalAccess(user);
    l.setMonthlyRent(new BigDecimal("1000.00"));
    l.setStartDate(LocalDate.of(2026, 10, 1));
    l.setEndDate(LocalDate.of(2027, 9, 30));
    return leases.saveAndFlush(l);
  }

  Invoice seedInvoice(Lease l) {
    var i = new Invoice();
    i.setLease(l);
    i.setAmount(new BigDecimal("1000.00"));
    i.setDueDate(LocalDate.of(2026, 10, 1));
    i.setBillingPeriod(LocalDate.of(2026, 10, 1));
    i.setDescription("October rent");
    i.setCreatedAt(LocalDateTime.now());
    return invoices.saveAndFlush(i);
  }

  Session paidSession(Payment p) {
    var s = new Session();
    s.setId("cs_test_" + p.getId());
    s.setPaymentIntent("pi_" + p.getId());
    s.setAmountTotal(p.getTotalAmount().movePointRight(2).longValueExact());
    s.setCurrency("usd");
    s.setLivemode(false);
    s.setStatus("complete");
    s.setPaymentStatus("paid");
    s.setMetadata(
        Map.of(
            "payment_id",
            p.getId().toString(),
            "invoice_id",
            p.getInvoice().getId().toString(),
            "company_id",
            company.getId().toString()));
    return s;
  }

  @Test
  void ownersCannotReadOrChangeAnotherCompany() {
    assertThatThrownBy(() -> properties.property(company.getId(), otherProperty.getId()))
        .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> leasing.managed(company.getId(), otherLease.getId()))
        .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(
            () ->
                transactions.offline(
                    company.getId(),
                    otherInvoice.getId(),
                    BigDecimal.TEN,
                    "CASH",
                    "receipt",
                    "received",
                    UUID.randomUUID().toString()))
        .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> access.member(otherCompany.getId()))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void sameUnitNumberWorksAcrossProperties() {
    assertThat(office.getOfficeNumber()).isEqualTo(otherOffice.getOfficeNumber());
    assertThatThrownBy(
            () ->
                properties.saveOffice(
                    company.getId(),
                    null,
                    property.getId(),
                    "101",
                    200,
                    "",
                    OfficeStatus.AVAILABLE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ownerRoutesAndTenantRoutesRender() throws Exception {
    for (String path :
        List.of(
            "/admin/dashboard",
            "/admin/properties",
            "/admin/leases",
            "/admin/lease/new",
            "/admin/invoices",
            "/admin/payments",
            "/admin/maintenance",
            "/admin/company",
            "/admin/leases/" + lease.getId() + "/documents")) {
      mvc.perform(get(path).session(session(company)).with(user(owner.getEmail()).roles("ADMIN")))
          .andExpect(status().isOk());
    }
    for (String path :
        List.of(
            "/tenant/dashboard",
            "/tenant/invoices",
            "/tenant/payments",
            "/tenant/lease",
            "/tenant/maintenance/new")) {
      mvc.perform(get(path).with(user(tenant.getEmail()).roles("TENANT")))
          .andExpect(status().isOk());
    }
  }

  @Test
  void listPagesExcludeOtherCompanies() throws Exception {
    mvc.perform(
            get("/admin/properties")
                .session(session(company))
                .with(user(owner.getEmail()).roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Building Two"))));
  }

  @Test
  void tenantCannotMarkAnotherInvoicePaid() throws Exception {
    mvc.perform(
            post("/pay/" + otherInvoice.getId())
                .with(user(tenant.getEmail()).roles("TENANT"))
                .with(csrf()))
        .andExpect(status().isNotFound());
    assertThat(otherInvoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    assertThat(payments.count()).isZero();
  }

  @Test
  void tenantCannotUseAdminRoutes() throws Exception {
    mvc.perform(get("/admin/dashboard").with(user(tenant.getEmail()).roles("TENANT")))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/admin/invoices/" + invoice.getId() + "/offline")
                .with(user(tenant.getEmail()).roles("TENANT"))
                .with(csrf())
                .param("amount", "1000")
                .param("method", "CASH")
                .param("reference", "x")
                .param("note", "x")
                .param("key", UUID.randomUUID().toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  void csrfIsRequiredForMutations() throws Exception {
    mvc.perform(post("/pay/" + invoice.getId()).with(user(tenant.getEmail()).roles("TENANT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void leaseOverlapAndNegativeRentAreRejected() {
    var f = new LeaseForm();
    f.setOfficeId(office.getId());
    f.setTenantName("Next Tenant");
    f.setTenantEmail("new@example.test");
    f.setMonthlyRent(new BigDecimal("1000"));
    f.setStartDate(LocalDate.of(2026, 11, 1));
    f.setEndDate(LocalDate.of(2027, 10, 31));
    assertThatThrownBy(() -> leasing.create(company.getId(), f))
        .hasMessageContaining("overlapping");
    f.setStartDate(LocalDate.of(2027, 10, 1));
    f.setMonthlyRent(new BigDecimal("-1"));
    assertThatThrownBy(() -> leasing.create(company.getId(), f)).hasMessageContaining("Rent");
  }

  @Test
  void monthlyGenerationIsIdempotentAndDoesNotBillBeforeStart() {
    billing.generate(lease.getId());
    billing.generate(lease.getId());
    assertThat(invoices.findByLeaseIdOrderByDueDate(lease.getId())).hasSize(2);
    otherLease.setStartDate(LocalDate.of(2027, 2, 1));
    billing.generate(otherLease.getId());
    assertThat(invoices.findByLeaseIdOrderByDueDate(otherLease.getId())).hasSize(1);
  }

  @Test
  void firstMonthIsProratedAndNotDueBeforeMoveIn() {
    var future = seedLease(office, tenant);
    future.setStartDate(LocalDate.of(2026, 10, 16));
    future.setEndDate(LocalDate.of(2027, 1, 31));
    future.setMonthlyRent(new BigDecimal("3100.00"));
    future.setProrateFirstMonth(true);
    leases.saveAndFlush(future);
    billing.generate(future.getId());
    var first = invoices.findByLeaseIdOrderByDueDate(future.getId()).get(0);
    assertThat(first.getAmount()).isEqualByComparingTo("1600.00");
    assertThat(first.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 16));
  }

  @Test
  void lateFeeIsAddedOnceAndSkippedDuringCheckout() {
    lease.setLateFee(new BigDecimal("25.00"));
    billing.updateOverdue(invoice.getId());
    billing.updateOverdue(invoice.getId());
    assertThat(invoice.getLateFeeAmount()).isEqualByComparingTo("25");
    login(otherTenant);
    when(stripe.account(otherCompany.getId())).thenReturn("acct_two");
    otherLease.setLateFee(new BigDecimal("40"));
    transactions.reserve(otherInvoice.getId());
    billing.updateOverdue(otherInvoice.getId());
    assertThat(otherInvoice.getLateFeeAmount()).isZero();
  }

  @Test
  void partialOfflinePaymentsTrackBalanceAndPreventRepeatSubmit() {
    var key = UUID.randomUUID().toString();
    transactions.offline(
        company.getId(),
        invoice.getId(),
        new BigDecimal("250"),
        "CHECK",
        "check-1",
        "Received at office",
        key);
    transactions.offline(
        company.getId(),
        invoice.getId(),
        new BigDecimal("250"),
        "CHECK",
        "check-1",
        "Received at office",
        key);
    assertThat(payments.count()).isEqualTo(1);
    assertThat(billing.balance(invoice)).isEqualByComparingTo("750");
    assertThatThrownBy(
            () ->
                transactions.offline(
                    company.getId(),
                    invoice.getId(),
                    new BigDecimal("751"),
                    "CASH",
                    "r",
                    "note",
                    UUID.randomUUID().toString()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void checkoutReservesSingleAttemptAndNeverMarksPaid() {
    login(tenant);
    var one = transactions.reserve(invoice.getId());
    var two = transactions.reserve(invoice.getId());
    assertThat(one.getId()).isEqualTo(two.getId());
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    assertThat(one.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  void confirmedPaymentsAreIdempotentAndReceiptsAreScoped() throws Exception {
    login(tenant);
    var p = transactions.reserve(invoice.getId());
    var s = paidSession(p);
    transactions.apply("evt_one", s, "acct_one", "checkout.session.completed");
    transactions.apply("evt_one", s, "acct_one", "checkout.session.completed");
    transactions.apply("evt_two", s, "acct_one", "checkout.session.async_payment_succeeded");
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    assertThat(billing.balance(invoice)).isZero();
    assertThat(
            outbox.findAll().stream().filter(m -> m.getSubject().equals("Payment receipt")).count())
        .isEqualTo(1);
    mvc.perform(get("/receipts/" + p.getId()).with(user(otherTenant.getEmail()).roles("TENANT")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/receipts/" + p.getId()).with(user(tenant.getEmail()).roles("TENANT")))
        .andExpect(status().isOk());
  }

  @Test
  void mismatchedAmountAccountOrModeCannotSettle() {
    login(tenant);
    var p = transactions.reserve(invoice.getId());
    var s = paidSession(p);
    s.setAmountTotal(1L);
    assertThatThrownBy(
            () -> transactions.apply("evt_bad", s, "acct_one", "checkout.session.completed"))
        .hasMessageContaining("mismatch");
    s.setAmountTotal(100000L);
    assertThatThrownBy(
            () -> transactions.apply("evt_bad", s, "acct_two", "checkout.session.completed"))
        .hasMessageContaining("mismatch");
    s.setLivemode(true);
    assertThatThrownBy(
            () -> transactions.apply("evt_bad", s, "acct_one", "checkout.session.completed"))
        .hasMessageContaining("mismatch");
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
  }

  @Test
  void asynchronousPaymentsRemainUnpaidUntilSuccess() {
    login(tenant);
    var p = transactions.reserve(invoice.getId());
    var s = paidSession(p);
    s.setPaymentStatus("unpaid");
    transactions.apply("evt_pending", s, "acct_one", "checkout.session.completed");
    assertThat(p.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    assertThatThrownBy(() -> transactions.reserve(invoice.getId()))
        .hasMessageContaining("processing");
    s.setPaymentStatus("paid");
    transactions.apply("evt_success", s, "acct_one", "checkout.session.async_payment_succeeded");
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
  }

  @Test
  void refundsReopenTheCorrectBalance() {
    login(tenant);
    var p = transactions.reserve(invoice.getId());
    transactions.apply("evt_paid", paidSession(p), "acct_one", "checkout.session.completed");
    var charge = new Charge();
    charge.setId("ch_one");
    charge.setPaymentIntent(p.getStripePaymentIntentId());
    charge.setLivemode(false);
    charge.setCurrency("usd");
    charge.setAmount(100000L);
    charge.setAmountRefunded(25000L);
    transactions.refund("evt_refund", charge, "acct_one");
    assertThat(billing.balance(invoice)).isEqualByComparingTo("250.00");
    transactions.refund("evt_refund", charge, "acct_one");
    assertThat(billing.balance(invoice)).isEqualByComparingTo("250.00");
  }

  @Test
  void invitationRequiresExistingTenantToSignInAndCannotBeReused() {
    invites.sendInvitation(lease);
    var message = outbox.findAll().get(0).getBody();
    String token = message.substring(message.lastIndexOf("token=") + 6);
    assertThat(invitationRepository.findAll().get(0).getToken()).isNotEqualTo(token);
    assertThatThrownBy(() -> invites.accept(token, "", "", "", otherTenant))
        .hasMessageContaining("Sign in");
    invites.accept(token, "", "", "", tenant);
    assertThat(lease.getPortalAccess().getId()).isEqualTo(tenant.getId());
    assertThatThrownBy(() -> invites.accept(token, "", "", "", tenant))
        .hasMessageContaining("already used");
  }

  @Test
  void maintenanceUsesAllLeasesAndUpdatesReachTenant() throws Exception {
    login(tenant);
    maintenance.create(lease.getId(), "AC issue", "Warm air", PriorityLevel.HIGH);
    assertThatThrownBy(
            () -> maintenance.create(otherLease.getId(), "AC", "bad", PriorityLevel.HIGH))
        .isInstanceOf(ResponseStatusException.class);
    var r = requests.findAll().get(0);
    login(owner);
    maintenance.update(
        company.getId(), r.getId(), MaintenanceRequestStatus.COMPLETED, "Technician fixed it");
    assertThat(r.getCompletedAt()).isNotNull();
    mvc.perform(get("/tenant/dashboard").with(user(tenant.getEmail()).roles("TENANT")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Technician fixed it")));
  }

  @Test
  void companyInvitesUseOwnerPermissionsAndLastOwnerCannotBeRemoved() {
    var membership =
        memberships.findByCompanyIdAndUserId(company.getId(), owner.getId()).orElseThrow();
    assertThatThrownBy(() -> companies.remove(company, membership.getId()))
        .hasMessageContaining("at least one owner");
    membership.setRole(MembershipRole.MANAGER);
    assertThatThrownBy(() -> companies.invite(company, "new@example.test", MembershipRole.OWNER))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void resetTokenIsSingleUse() {
    resets.request(tenant.getEmail());
    var body = outbox.findAll().get(0).getBody();
    String token = body.substring(body.lastIndexOf("token=") + 6);
    resets.reset(token, "a-new-secure-password");
    assertThatThrownBy(() -> resets.reset(token, "another-secure-password"))
        .hasMessageContaining("already used");
  }

  @Test
  void signedWebhooksSettlePaymentsAndRejectTampering() throws Exception {
    login(tenant);
    var p = transactions.reserve(invoice.getId());
    var session = paidSession(p);
    when(stripe.retrieve(session.getId(), "acct_one")).thenReturn(session);
    String payload =
        "{\"id\":\"evt_signed\",\"type\":\"checkout.session.completed\",\"account\":\"acct_one\",\"livemode\":false,\"data\":{\"object\":{\"id\":\""
            + session.getId()
            + "\",\"object\":\"checkout.session\"}}}";
    long now = Instant.now().getEpochSecond();
    var mac = javax.crypto.Mac.getInstance("HmacSHA256");
    mac.init(
        new javax.crypto.spec.SecretKeySpec(
            "whsec_local_test_secret".getBytes(java.nio.charset.StandardCharsets.UTF_8),
            "HmacSHA256"));
    String signature =
        "t="
            + now
            + ",v1="
            + java.util.HexFormat.of()
                .formatHex(
                    mac.doFinal(
                        (now + "." + payload).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    mvc.perform(
            post("/webhooks/stripe")
                .with(anonymous())
                .contentType("application/json")
                .content(payload)
                .header("Stripe-Signature", signature))
        .andExpect(status().isOk());
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    mvc.perform(
            post("/webhooks/stripe")
                .with(anonymous())
                .contentType("application/json")
                .content(payload + " ")
                .header("Stripe-Signature", signature))
        .andExpect(status().isBadRequest());
  }

  @Test
  void documentsAreLimitedToTheLeaseParties() throws Exception {
    var pdf =
        new org.springframework.mock.web.MockMultipartFile(
            "file", "lease.pdf", "application/pdf", "%PDF-1.4 test".getBytes());
    documentService.upload(company.getId(), lease.getId(), pdf);
    var document = documents.findByLeaseIdOrderByCreatedAtDesc(lease.getId()).get(0);
    mvc.perform(get("/documents/" + document.getId()).with(user(tenant.getEmail()).roles("TENANT")))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/pdf"));
    mvc.perform(
            get("/documents/" + document.getId())
                .with(user(otherTenant.getEmail()).roles("TENANT")))
        .andExpect(status().isForbidden());
    login(otherOwner);
    assertThatThrownBy(() -> documentService.upload(otherCompany.getId(), lease.getId(), pdf))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void maintenancePhotosAreValidatedAndPrivate() throws Exception {
    login(tenant);
    maintenance.create(lease.getId(), "Leak", "Pipe leaking", PriorityLevel.HIGH);
    var request = requests.findAll().get(0);
    var bytes = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(
        new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        bytes);
    photoService.upload(
        request.getId(),
        new org.springframework.mock.web.MockMultipartFile(
            "file", "photo.png", "image/png", bytes.toByteArray()));
    var photo = photos.findByRequestIdOrderById(request.getId()).get(0);
    mvc.perform(
            get("/maintenance-photos/" + photo.getId())
                .with(user(tenant.getEmail()).roles("TENANT")))
        .andExpect(status().isOk());
    mvc.perform(
            get("/maintenance-photos/" + photo.getId())
                .with(user(otherTenant.getEmail()).roles("TENANT")))
        .andExpect(status().isForbidden());
    login(tenant);
    assertThatThrownBy(
            () ->
                photoService.upload(
                    request.getId(),
                    new org.springframework.mock.web.MockMultipartFile(
                        "file", "bad.png", "image/png", "<script>bad</script>".getBytes())))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void existingTenantSeesOfflinePaymentsRecordedBeforeInvitationAcceptance() {
    lease.setPortalAccess(null);
    transactions.offline(
        company.getId(),
        invoice.getId(),
        new BigDecimal("1000"),
        "CHECK",
        "check-first",
        "Paid before registration",
        UUID.randomUUID().toString());
    lease.setPortalAccess(tenant);
    leases.saveAndFlush(lease);
    assertThat(payments.findByUserIdOrderByCreatedAtDesc(tenant.getId())).hasSize(1);
  }

  @Test
  void futureInvoiceVoidingAndRenewalDoNotLoseHistory() {
    billing.generate(lease.getId());
    leasing.terminate(company.getId(), lease.getId(), LocalDate.of(2026, 10, 31));
    var bills = invoices.findByLeaseIdOrderByDueDate(lease.getId());
    assertThat(bills.get(0).getStatus()).isNotEqualTo(InvoiceStatus.VOID);
    assertThat(bills.get(1).getStatus()).isEqualTo(InvoiceStatus.VOID);
  }

  @Test
  void historicalLeaseOnlyBillsFromSelectedStartMonth() {
    lease.setStartDate(LocalDate.of(2024, 1, 15));
    lease.setBillingStartDate(LocalDate.of(2026, 10, 1));
    lease.setProrateFirstMonth(true);
    leases.saveAndFlush(lease);
    billing.generate(lease.getId());
    assertThat(invoices.findByLeaseIdOrderByDueDate(lease.getId())).hasSize(2);
    assertThat(invoices.findByLeaseIdOrderByDueDate(lease.getId()).get(1).getAmount())
        .isEqualByComparingTo("1000.00");
  }

  @Test
  void checkoutAttachmentPreservesAlreadyConfirmedPayment() {
    login(tenant);
    var p = transactions.reserve(invoice.getId());
    var session = paidSession(p);
    transactions.apply("evt_early", session, "acct_one", "checkout.session.completed");
    transactions.attach(p.getId(), session);
    assertThat(payments.findById(p.getId()).orElseThrow().getStatus())
        .isEqualTo(PaymentStatus.PAID);
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
  }
}
