package com.victorpena.plaza.service;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.repository.InvoiceRepository;
import com.victorpena.plaza.repository.LeaseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class InvoiceGenerationService {
	
	private final LeaseRepository leaseRepository;
	private final InvoiceRepository invoiceRepository;
	
	public InvoiceGenerationService(LeaseRepository leaseRepository, InvoiceRepository invoiceRepository) {
		this.leaseRepository = leaseRepository;
		this.invoiceRepository = invoiceRepository;
	}

	@Scheduled(cron = "0 0 1 * * *")
	public void generateInvoices() {

	    LocalDate nextMonth =
	            LocalDate.now()
	                     .plusMonths(1)
	                     .withDayOfMonth(1);

	    List<Lease> activeLeases =
	            leaseRepository.findByActiveTrue();

	    for (Lease lease : activeLeases) {

	        if (nextMonth.isAfter(lease.getEndDate())) {
	            continue;
	        }

	        boolean exists =
	            invoiceRepository.existsByLeaseAndDueDate(
	                lease,
	                nextMonth
	            );

	        if (!exists) {

	            Invoice invoice = new Invoice();

	            invoice.setLease(lease);
	            invoice.setAmount(
	                lease.getMonthlyRent()
	            );
	            invoice.setDueDate(nextMonth);
	            invoice.setStatus(
	                InvoiceStatus.PENDING
	            );
	            
	            invoice.setCreatedAt(
	                    LocalDateTime.now()
	                );


	            invoiceRepository.save(invoice);
	        }
	    }
	}
	
	@Scheduled(cron = "0 30 1 * * *")
	public void markOverdueInvoices() {

	    List<Invoice> invoices =
	            invoiceRepository.findByStatus(
	                InvoiceStatus.PENDING);

	    for (Invoice invoice : invoices) {

	        if (invoice.getDueDate()
	                .isBefore(LocalDate.now())) {

	            invoice.setStatus(
	                InvoiceStatus.OVERDUE);

	            invoiceRepository.save(invoice);
	        }
	    }
	}
}
