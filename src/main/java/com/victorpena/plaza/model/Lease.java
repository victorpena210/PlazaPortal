package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "leases")
public class Lease {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  private User portalAccess;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "office_id", nullable = false)
  private Office office;

  @Column(length = 255)
  private String tenantName;

  @Column(length = 255)
  private String tenantEmail;

  @Column(precision = 10, scale = 2)
  private BigDecimal monthlyRent;

  private LocalDate startDate;

  private LocalDate endDate;

  private boolean active = true;

  @Column(nullable = false)
  private int dueDay = 1;

  @Column(nullable = false)
  private boolean prorateFirstMonth = false;

  @Column(nullable = false)
  private int graceDays = 0;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal lateFee = BigDecimal.ZERO;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public User getPortalAccess() {
    return portalAccess;
  }

  public void setPortalAccess(User value) {
    this.portalAccess = value;
  }

  public Office getOffice() {
    return office;
  }

  public void setOffice(Office value) {
    this.office = value;
  }

  public String getTenantName() {
    return tenantName;
  }

  public void setTenantName(String value) {
    this.tenantName = value;
  }

  public String getTenantEmail() {
    return tenantEmail;
  }

  public void setTenantEmail(String value) {
    this.tenantEmail = value;
  }

  public BigDecimal getMonthlyRent() {
    return monthlyRent;
  }

  public void setMonthlyRent(BigDecimal value) {
    this.monthlyRent = value;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate value) {
    this.startDate = value;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate value) {
    this.endDate = value;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }

  public int getDueDay() {
    return dueDay;
  }

  public void setDueDay(int value) {
    this.dueDay = value;
  }

  public boolean isProrateFirstMonth() {
    return prorateFirstMonth;
  }

  public void setProrateFirstMonth(boolean value) {
    this.prorateFirstMonth = value;
  }

  public int getGraceDays() {
    return graceDays;
  }

  public void setGraceDays(int value) {
    this.graceDays = value;
  }

  public BigDecimal getLateFee() {
    return lateFee;
  }

  public void setLateFee(BigDecimal value) {
    this.lateFee = value;
  }

  private LocalDate billingStartDate;

  public LocalDate getBillingStartDate() {
    return billingStartDate;
  }

  public void setBillingStartDate(LocalDate value) {
    this.billingStartDate = value;
  }
}
