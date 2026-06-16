package com.victorpena.plaza.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> findByTenantId(Long userId) {
        return invoiceRepository.findByLeasePortalAccess_Id(userId);
    }

    public Invoice findById(Long invoiceId) {
        return invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invoice not found: " + invoiceId));
    }

	public void createInitialInvoice(Lease lease) {
	    Invoice invoice = new Invoice();

	    invoice.setLease(lease);
	    invoice.setAmount(lease.getMonthlyRent());

	    invoice.setDueDate(
	            lease.getStartDate()
	                 .withDayOfMonth(1));

	    invoice.setStatus(InvoiceStatus.PENDING);
	    invoice.setCreatedAt(LocalDateTime.now());

	    invoiceRepository.save(invoice);		
	}
}