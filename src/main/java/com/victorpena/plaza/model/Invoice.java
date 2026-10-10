package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(
    name = "invoice",
    uniqueConstraints = @UniqueConstraint(columnNames = {"lease_id", "billing_period", "kind"}))
public class Invoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lease_id", nullable = false)
  private Lease lease;

  @Column(precision = 10, scale = 2)
  private BigDecimal amount;

  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private InvoiceStatus status = InvoiceStatus.PENDING;

  private LocalDateTime createdAt;

  private LocalDate billingPeriod;

  @Column(nullable = false, length = 20)
  private String kind = "RENT";

  @Column(length = 255)
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal lateFeeAmount = BigDecimal.ZERO;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Lease getLease() {
    return lease;
  }

  public void setLease(Lease value) {
    this.lease = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate value) {
    this.dueDate = value;
  }

  public InvoiceStatus getStatus() {
    return status;
  }

  public void setStatus(InvoiceStatus value) {
    this.status = value;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime value) {
    this.createdAt = value;
  }

  public LocalDate getBillingPeriod() {
    return billingPeriod;
  }

  public void setBillingPeriod(LocalDate value) {
    this.billingPeriod = value;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String value) {
    this.kind = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public BigDecimal getLateFeeAmount() {
    return lateFeeAmount;
  }

  public void setLateFeeAmount(BigDecimal value) {
    this.lateFeeAmount = value;
  }

  public BigDecimal getTotal() {
    return amount.add(lateFeeAmount);
  }
}
