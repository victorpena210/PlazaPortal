package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "payments")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "office_id", nullable = false)
  private Office office;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lease_id", nullable = true)
  private Lease lease;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = true)
  private Invoice invoice;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(precision = 10, scale = 2)
  private BigDecimal lateFee = BigDecimal.ZERO;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal refundedAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentStatus status;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private LocalDateTime paidAt;

  @Column(length = 7)
  private String paymentMonth;

  @Column(length = 50)
  private String paymentMethod;

  @Column(length = 255)
  private String transactionReference;

  @Column(length = 255)
  private String receiptUrl;

  @Column(unique = true, length = 64)
  private String attemptKey;

  @Column(length = 255)
  private String stripeSessionId;

  @Column(length = 255)
  private String stripePaymentIntentId;

  @Column(length = 255)
  private String stripeAccountId;

  @Column(length = 2048)
  private String checkoutUrl;

  private LocalDateTime checkoutExpiresAt;

  @Column(length = 500)
  private String note;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    this.user = value;
  }

  public Office getOffice() {
    return office;
  }

  public void setOffice(Office value) {
    this.office = value;
  }

  public Lease getLease() {
    return lease;
  }

  public void setLease(Lease value) {
    this.lease = value;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public void setInvoice(Invoice value) {
    this.invoice = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal value) {
    this.totalAmount = value;
  }

  public BigDecimal getLateFee() {
    return lateFee;
  }

  public void setLateFee(BigDecimal value) {
    this.lateFee = value;
  }

  public BigDecimal getRefundedAmount() {
    return refundedAmount;
  }

  public void setRefundedAmount(BigDecimal value) {
    this.refundedAmount = value;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus value) {
    this.status = value;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime value) {
    this.createdAt = value;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime value) {
    this.updatedAt = value;
  }

  public LocalDateTime getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(LocalDateTime value) {
    this.paidAt = value;
  }

  public String getPaymentMonth() {
    return paymentMonth;
  }

  public void setPaymentMonth(String value) {
    this.paymentMonth = value;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String value) {
    this.paymentMethod = value;
  }

  public String getTransactionReference() {
    return transactionReference;
  }

  public void setTransactionReference(String value) {
    this.transactionReference = value;
  }

  public String getReceiptUrl() {
    return receiptUrl;
  }

  public void setReceiptUrl(String value) {
    this.receiptUrl = value;
  }

  public String getAttemptKey() {
    return attemptKey;
  }

  public void setAttemptKey(String value) {
    this.attemptKey = value;
  }

  public String getStripeSessionId() {
    return stripeSessionId;
  }

  public void setStripeSessionId(String value) {
    this.stripeSessionId = value;
  }

  public String getStripePaymentIntentId() {
    return stripePaymentIntentId;
  }

  public void setStripePaymentIntentId(String value) {
    this.stripePaymentIntentId = value;
  }

  public String getStripeAccountId() {
    return stripeAccountId;
  }

  public void setStripeAccountId(String value) {
    this.stripeAccountId = value;
  }

  public String getCheckoutUrl() {
    return checkoutUrl;
  }

  public void setCheckoutUrl(String value) {
    this.checkoutUrl = value;
  }

  public LocalDateTime getCheckoutExpiresAt() {
    return checkoutExpiresAt;
  }

  public void setCheckoutExpiresAt(LocalDateTime value) {
    this.checkoutExpiresAt = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    this.note = value;
  }

  @PrePersist
  public void created() {
    if (createdAt == null) createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void updated() {
    updatedAt = LocalDateTime.now();
  }
}
