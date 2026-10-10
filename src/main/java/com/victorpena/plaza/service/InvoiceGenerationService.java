package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class InvoiceGenerationService {
  private final LeaseRepository leases;
  private final InvoiceRepository invoices;
  private final OfficeRepository offices;
  private final BillingService billing;
  private final boolean enabled;
  private static final Logger log = LoggerFactory.getLogger(InvoiceGenerationService.class);

  public InvoiceGenerationService(
      LeaseRepository leases,
      InvoiceRepository invoices,
      OfficeRepository offices,
      BillingService billing,
      @Value("${app.jobs-enabled:true}") boolean enabled) {
    this.leases = leases;
    this.invoices = invoices;
    this.offices = offices;
    this.billing = billing;
    this.enabled = enabled;
  }

  @Scheduled(cron = "0 0 1 * * *", zone = "${app.time-zone:America/Chicago}")
  public void runDaily() {
    if (!enabled) return;
    for (var l : leases.findByActiveTrue()) {
      try {
        billing.generate(l.getId());
      } catch (Exception e) {
        log.error("Invoice generation failed for lease {}", l.getId(), e);
      }
    }
    for (var i : invoices.findAll()) {
      try {
        billing.updateOverdue(i.getId());
      } catch (Exception e) {
        log.error("Invoice update failed for {}", i.getId(), e);
      }
    }
    for (var o : offices.findAll()) {
      boolean occupied = !leases.overlapping(o.getId(), billing.today(), billing.today()).isEmpty();
      if (occupied) o.setStatus(OfficeStatus.OCCUPIED);
      else if (o.getStatus() == OfficeStatus.OCCUPIED) o.setStatus(OfficeStatus.AVAILABLE);
      offices.save(o);
    }
  }
}
