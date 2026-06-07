package com.victorpena.plaza.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.OfficeStatus;
import com.victorpena.plaza.repository.OfficeRepository;

@Service
public class OfficeService {
	
	private final OfficeRepository officeRepository;
	
	public OfficeService(OfficeRepository officeRepository) {
		this.officeRepository = officeRepository;
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
	
	
	public void markOfficeAsMaintenance(Long officeId) {
		Office office = officeRepository.findById(officeId)
				.orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));
		if (office.getStatus() == OfficeStatus.OCCUPIED) {
			throw new IllegalArgumentException("Occupied office cannot be marked as maintenance.");
		}
		
		office.setStatus(OfficeStatus.MAINTENANCE);
		officeRepository.save(office);
	}
	
	public void markOfficeAsAvailable(Long officeId) {
	    Office office = officeRepository.findById(officeId)
	            .orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));

	    if (office.getStatus() == OfficeStatus.OCCUPIED) {
	        throw new IllegalArgumentException(
	            "Occupied office cannot be marked as available.");
	    }

	    office.setStatus(OfficeStatus.AVAILABLE);
	    officeRepository.save(office);
	}
	

	
	
}