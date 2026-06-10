package com.victorpena.plaza.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "offices")
public class Office {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "office_number", nullable = false, unique = true)
	private String officeNumber;
	
	@Column(name = "square_feet")
	private Integer squareFeet;
	
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OfficeStatus status;
	
	@Column(length = 255)
	private String description;

	
	public Office() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOfficeNumber() {
		return officeNumber;
	}

	public void setOfficeNumber(String officeNumber) {
		this.officeNumber = officeNumber;
	}

	public Integer getSquareFeet() {
		return squareFeet;
	}

	public void setSquareFeet(Integer squareFeet) {
		this.squareFeet = squareFeet;
	}


	public OfficeStatus getStatus() {
		return status;
	}

	public void setStatus(OfficeStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
