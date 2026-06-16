package com.victorpena.plaza.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.web.LeaseForm;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final OfficeRepository officeRepository;
    private final InvoiceService invoiceService;
    private final InvitationService invitationService;

    public LeaseService(
            LeaseRepository leaseRepository,
            OfficeRepository officeRepository,
            InvoiceService invoiceService,
            InvitationService invitationService) {

        this.leaseRepository = leaseRepository;
        this.officeRepository = officeRepository;
        this.invoiceService = invoiceService;
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
        
        invoiceService.createInitialInvoice(lease);

        if (lease.getTenantEmail() != null
                && !lease.getTenantEmail().isBlank()) {

            invitationService.sendInvitation(lease);
        }
      
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
    
    public Lease findActiveLeaseByTenantId(Long tenantId) {

        return leaseRepository
                .findByPortalAccess_IdAndActiveTrue(tenantId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<Lease> findAll() {
        return leaseRepository.findAll();
    }

}