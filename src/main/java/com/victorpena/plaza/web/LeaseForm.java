package com.victorpena.plaza.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaseForm {
  private Long officeId;
  private String tenantName;
  private String tenantEmail;
  private BigDecimal monthlyRent;
  private LocalDate startDate;
  private LocalDate endDate;
  private int dueDay = 1;
  private boolean prorateFirstMonth;
  private int graceDays;
  private BigDecimal lateFee = BigDecimal.ZERO;

  public Long getOfficeId() {
    return officeId;
  }

  public void setOfficeId(Long v) {
    officeId = v;
  }

  public String getTenantName() {
    return tenantName;
  }

  public void setTenantName(String v) {
    tenantName = v;
  }

  public String getTenantEmail() {
    return tenantEmail;
  }

  public void setTenantEmail(String v) {
    tenantEmail = v;
  }

  public BigDecimal getMonthlyRent() {
    return monthlyRent;
  }

  public void setMonthlyRent(BigDecimal v) {
    monthlyRent = v;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate v) {
    startDate = v;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate v) {
    endDate = v;
  }

  public int getDueDay() {
    return dueDay;
  }

  public void setDueDay(int v) {
    dueDay = v;
  }

  public boolean isProrateFirstMonth() {
    return prorateFirstMonth;
  }

  public void setProrateFirstMonth(boolean v) {
    prorateFirstMonth = v;
  }

  public int getGraceDays() {
    return graceDays;
  }

  public void setGraceDays(int v) {
    graceDays = v;
  }

  public BigDecimal getLateFee() {
    return lateFee;
  }

  public void setLateFee(BigDecimal v) {
    lateFee = v;
  }

  private LocalDate billingStartDate;

  public LocalDate getBillingStartDate() {
    return billingStartDate;
  }

  public void setBillingStartDate(LocalDate value) {
    this.billingStartDate = value;
  }
}
