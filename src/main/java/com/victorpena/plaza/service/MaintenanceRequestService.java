package com.victorpena.plaza.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.MaintenanceRequest;
import com.victorpena.plaza.model.MaintenanceRequestStatus;
import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.MaintenanceRequestRepository;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.UserRepository;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final OfficeRepository officeRepository;
    private final UserRepository userRepository;

    public MaintenanceRequestService(
            MaintenanceRequestRepository maintenanceRequestRepository,
            OfficeRepository officeRepository,
            UserRepository userRepository) {
    	this.maintenanceRequestRepository = maintenanceRequestRepository;
    	this.officeRepository = officeRepository;
    	this.userRepository = userRepository;
    }

    public MaintenanceRequest createRequest(Long userId, Long officeId, String title, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Office office = officeRepository.findById(officeId)
                .orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));

        if (office.getUser() == null || !office.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only submit requests for your assigned office.");
        }

        MaintenanceRequest request = new MaintenanceRequest();
        request.setUser(user);
        request.setOffice(office);
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