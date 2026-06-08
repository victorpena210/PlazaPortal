package com.victorpena.plaza.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.MaintenanceRequest;
import com.victorpena.plaza.model.MaintenanceRequestStatus;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.MaintenanceRequestRepository;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final LeaseRepository leaseRepository;

    public MaintenanceRequestService(
            MaintenanceRequestRepository maintenanceRequestRepository,
            LeaseRepository leaseRepository) {
    	this.maintenanceRequestRepository = maintenanceRequestRepository;
    	this.leaseRepository = leaseRepository;
    }

    public MaintenanceRequest createRequest(Long userId, Long officeId, String title, String description) {
    	Lease lease = leaseRepository
    	        .findByTenantIdAndOfficeIdAndActiveTrue(userId, officeId)
    	        .orElseThrow(() ->
    	                new RuntimeException("Active lease not found"));


        MaintenanceRequest request = new MaintenanceRequest();
        request.setLease(lease);
        request.setUser(lease.getTenant());
        request.setOffice(lease.getOffice());
        request.setTitle(title.trim());
        request.setDescription(description.trim());
        request.setStatus(MaintenanceRequestStatus.OPEN);

        return maintenanceRequestRepository.save(request);
    }

    public List<MaintenanceRequest> findByUserId(Long userId) {
        return maintenanceRequestRepository.findByUserId(userId);
    }

    public List<MaintenanceRequest> findAll() {
        return maintenanceRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public MaintenanceRequest findById(Long id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance request not found: " + id));
    }

    public void updateStatus(Long requestId, MaintenanceRequestStatus status) {
        MaintenanceRequest request = findById(requestId);
        request.setStatus(status);
        maintenanceRequestRepository.save(request);
    }
}