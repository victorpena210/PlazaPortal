package com.victorpena.plaza.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.InvoiceRepository;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.UserRepository;
import com.victorpena.plaza.web.LeaseForm;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final InvoiceRepository invoiceRepository;

    public LeaseService(
            LeaseRepository leaseRepository,
            UserRepository userRepository,
            OfficeRepository officeRepository,
            InvoiceRepository invoiceRepository) {

        this.leaseRepository = leaseRepository;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public Lease createLease(LeaseForm form) {

        User tenant =
                userRepository.findById(form.getTenantId())
                        .orElseThrow();

        Office office =
                officeRepository.findById(form.getOfficeId())
                        .orElseThrow();

        Lease lease = new Lease();

        lease.setTenant(tenant);
        lease.setOffice(office);
        lease.setMonthlyRent(form.getMonthlyRent());
        lease.setStartDate(form.getStartDate());
        lease.setEndDate(form.getEndDate());
        lease.setActive(true);

        lease = leaseRepository.save(lease);

        Invoice invoice = new Invoice();

        invoice.setLease(lease);
        invoice.setAmount(lease.getMonthlyRent());

        invoice.setDueDate(
                lease.getStartDate()
                     .withDayOfMonth(1));

        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);

        return lease;
    }
}