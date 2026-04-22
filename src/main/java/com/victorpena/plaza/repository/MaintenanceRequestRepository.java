package com.victorpena.plaza.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.MaintenanceRequest;
import com.victorpena.plaza.model.MaintenanceRequestStatus;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
	
	List<MaintenanceRequest> findByUserId(Long userId);
	
	List<MaintenanceRequest> findByOfficeId(Long officeId);
	
	List<MaintenanceRequest> findByStatus(MaintenanceRequestStatus status);
	
    List<MaintenanceRequest> findAllByOrderByCreatedAtDesc();


		

}
