package com.victorpena.plaza.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	

	@Column(name = "paid_at")
	private LocalDateTime paidAt;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "office_id", nullable = false)
	private Office office;

	@Column(name = "payment_month", nullable = false, length = 7)
	private String paymentMonth;
	
	@Column(name = "stripe_session_id")
	private String stripeSessionId;
	
	public Payment() {
		
	}
	
	@PrePersist
	public void prePersist() {
	    if (createdAt == null) {
	        createdAt = LocalDateTime.now();
	    }
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Office getOffice() {
		return office;
	}

	public void setOffice(Office office) {
		this.office = office;
	}
	
	public String getPaymentMonth() {
	    return paymentMonth;
	}

	public void setPaymentMonth(String paymentMonth) {
	    this.paymentMonth = paymentMonth;
	}

	public String getStripeSessionId() {
		return stripeSessionId;
	}

	public void setStripeSessionId(String stripeSessionId) {
		this.stripeSessionId = stripeSessionId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	
}
