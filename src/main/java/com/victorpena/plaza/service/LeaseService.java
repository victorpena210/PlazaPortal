package com.victorpena.plaza.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.repository.InvoiceRepository;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.web.LeaseForm;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final OfficeRepository officeRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvitationService invitationService;

    public LeaseService(
            LeaseRepository leaseRepository,
            OfficeRepository officeRepository,
            InvoiceRepository invoiceRepository, InvitationService invitationService) {

        this.leaseRepository = leaseRepository;
        this.officeRepository = officeRepository;
        this.invoiceRepository = invoiceRepository;
        this.invitationService = invitationService;
    }

    public Lease createLease(LeaseForm form) {



        Office office =
                officeRepository.findById(form.getOfficeId())
                        .orElseThrow();

        Lease lease = new Lease();

        lease.setTenantName(form.getTenantName());
        lease.setTenantEmail(form.getTenantEmail());
        lease.setPortalAccess(null);
        lease.setOffice(office);
        lease.setMonthlyRent(form.getMonthlyRent());
        lease.setStartDate(form.getStartDate());
        lease.setEndDate(form.getEndDate());
        lease.setActive(true);

        lease = leaseRepository.save(lease);

        Invoice invoice = new Invoice();
        
        if (lease.getTenantEmail() != null
                && !lease.getTenantEmail().isBlank()) {

            invitationService.sendInvitation(lease);
        }
        
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
    
    public List<Office> findOfficesByTenantId(Long tenantId) {
        return leaseRepository.findByPortalAccess_Id(tenantId)
                .stream()
                .map(Lease::getOffice)
                .toList();
    }
    
    public List<Lease> findLeasesByTenantId(Long tenantId) {
        return leaseRepository.findByPortalAccess_IdAndActiveTrue(tenantId);
    }
}