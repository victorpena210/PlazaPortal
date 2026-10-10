package com.victorpena.plaza;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
    properties =
        "spring.datasource.url=jdbc:h2:mem:concurrency;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConcurrencyTest {
  @Autowired PlatformTransactionManager tx;
  @Autowired UserService users;
  @Autowired CompanyService companies;
  @Autowired PropertyRepository properties;
  @Autowired OfficeRepository offices;
  @Autowired LeaseRepository leases;
  @Autowired InvoiceRepository invoices;
  @Autowired PaymentRepository payments;
  @Autowired BillingService billing;
  @Autowired PaymentTransactions transactions;
  @MockitoBean StripeGateway stripe;
  Long leaseId;
  Long companyId;
  String email;

  @BeforeEach
  void seed() {
    new TransactionTemplate(tx)
        .executeWithoutResult(
            status -> {
              email = "concurrent-" + UUID.randomUUID() + "@example.test";
              var u =
                  users.registerUser("Test", "Owner", email, "a-long-test-password", Role.ADMIN);
              var c = companies.create(u, "Concurrency test");
              companyId = c.getId();
              var p = new Property();
              p.setCompany(c);
              p.setName("Building");
              p.setAddress("1 Test Road");
              properties.save(p);
              var o = new Office();
              o.setProperty(p);
              o.setOfficeNumber("1");
              offices.save(o);
              var l = new Lease();
              l.setOffice(o);
              l.setPortalAccess(u);
              l.setTenantEmail(email);
              l.setTenantName("Test");
              l.setMonthlyRent(new BigDecimal("1000"));
              l.setStartDate(billing.today().withDayOfMonth(1));
              l.setEndDate(billing.today().plusYears(1));
              leases.save(l);
              leaseId = l.getId();
            });
    when(stripe.account(companyId)).thenReturn("acct_test");
  }

  <T> List<T> parallel(Callable<T> action) throws Exception {
    var executor = Executors.newFixedThreadPool(2);
    var barrier = new CyclicBarrier(2);
    try {
      Callable<T> job =
          () -> {
            SecurityContextHolder.getContext()
                .setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(email, "", List.of()));
            try {
              barrier.await(5, TimeUnit.SECONDS);
              return action.call();
            } finally {
              SecurityContextHolder.clearContext();
            }
          };
      var one = executor.submit(job);
      var two = executor.submit(job);
      return List.of(one.get(20, TimeUnit.SECONDS), two.get(20, TimeUnit.SECONDS));
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void simultaneousBillingRunsCreateOneInvoicePerMonth() throws Exception {
    parallel(
        () -> {
          billing.generate(leaseId);
          return true;
        });
    assertThat(invoices.findByLeaseIdOrderByDueDate(leaseId)).hasSize(2);
  }

  @Test
  void simultaneousCheckoutClicksReserveOnePayment() throws Exception {
    billing.generate(leaseId);
    Long invoiceId = invoices.findByLeaseIdOrderByDueDate(leaseId).get(0).getId();
    var ids = parallel(() -> transactions.reserve(invoiceId).getId());
    assertThat(ids.get(0)).isEqualTo(ids.get(1));
    assertThat(payments.findByInvoiceIdOrderById(invoiceId)).hasSize(1);
  }
}
