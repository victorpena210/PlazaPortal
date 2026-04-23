package com.victorpena.plaza.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.OfficeStatus;
import com.victorpena.plaza.model.Role;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.UserRepository;

@Service
public class OfficeService {
	
	private final OfficeRepository officeRepository;
	private final UserRepository userRepository;
	
	public OfficeService(OfficeRepository officeRepository, UserRepository userRepository) {
		this.officeRepository = officeRepository;
		this.userRepository = userRepository;
	}
	
	public List<Office> findAll() {
		return officeRepository.findAll();
	}
	
	public List<Office> findAvailableOffices() {
		return officeRepository.findByStatus(OfficeStatus.AVAILABLE);
	}
	
	public List<Office> findOccupiedOffices() {
		return officeRepository.findByStatus(OfficeStatus.OCCUPIED);
	}
	
	public List<Office> findByUserId(Long userId) {
		return officeRepository.findByUserId(userId);
	}
	
	public List<Office> findMaintenanceOffices() {
	    return officeRepository.findByStatus(OfficeStatus.MAINTENANCE);
	}
	
	public long countAllOffices() {
		return officeRepository.count();
	}
	
	public long countAvailableOffices() {
		return officeRepository.findByStatus(OfficeStatus.AVAILABLE).size();
	}
	
	public long countOccupiedOffices() {
		return officeRepository.findByStatus(OfficeStatus.OCCUPIED).size();
	}
	
	public long countMaintenanceOffices() {
	    return officeRepository.findByStatus(OfficeStatus.MAINTENANCE).size();
	}
	
	public void assignOfficeToTenant(Long officeId, Long userId) {
		Office office = officeRepository.findById(officeId)
				.orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
		
		if (user.getRole() != Role.TENANT) {
			throw new IllegalArgumentException("Only tenants can be assigned to offices.");
		}
		
		if (office.getStatus() == OfficeStatus.MAINTENANCE) {
			throw new IllegalArgumentException("Office in maintenance cannot be assigned.");
		}
		
		if (office.getUser() != null || office.getStatus() == OfficeStatus.OCCUPIED) {
			throw new IllegalArgumentException("Office is already assigned.");
		}
		
		office.setUser(user);
		office.setStatus(OfficeStatus.OCCUPIED);
		
		officeRepository.save(office);
	}
	
	public void unassignOffice(Long officeId) {
		Office office = officeRepository.findById(officeId)
				.orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));
		
		office.setUser(null);
		office.setStatus(OfficeStatus.AVAILABLE);
		
		officeRepository.save(office);
	}
	
	public void markOfficeAsMaintenance(Long officeId) {
		Office office = officeRepository.findById(officeId)
				.orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));
		if(office.getUser() != null || office.getStatus() == OfficeStatus.OCCUPIED) {
			throw new IllegalArgumentException("Occupied office cannot be marked as maintenance.");
		}
		
		office.setStatus(OfficeStatus.MAINTENANCE);
		officeRepository.save(office);
	}
	
	public void markOfficeAsAvailable(Long officeId) {
	    Office office = officeRepository.findById(officeId)
	            .orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));

	    if (office.getUser() != null) {
	        throw new IllegalArgumentException("Assigned office cannot be marked as available manually.");
	    }

	    office.setStatus(OfficeStatus.AVAILABLE);
	    officeRepository.save(office);
	}
	

	
	
}