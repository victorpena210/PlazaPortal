package com.victorpena.plaza.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "leases")
public class Lease {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// portal account (nullable until registration)
	@ManyToOne
	@JoinColumn(name = "tenant_id")
	private User portalAccess;
    
    private String tenantName;
    
    private String tenantEmail;
    

    @ManyToOne
    private Office office;

    private BigDecimal monthlyRent;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public void setOffice(Office office) {
		this.office = office;
	}
	
	public Office getOffice() {
		return office;
	}
    
	

	public BigDecimal getMonthlyRent() {
		return monthlyRent;
	}

	public void setMonthlyRent(BigDecimal monthlyRent) {
		this.monthlyRent = monthlyRent;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public User getPortalAccess() {
		return portalAccess;
	}

	public void setPortalAccess(User portalAccess) {
		this.portalAccess = portalAccess;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantEmail() {
		return tenantEmail;
	}

	public void setTenantEmail(String tenantEmail) {
		this.tenantEmail = tenantEmail;
	}


	
}
