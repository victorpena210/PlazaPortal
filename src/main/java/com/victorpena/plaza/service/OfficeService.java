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

}
