package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.Payment;
import com.victorpena.plaza.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Payment> findAllByOrderByCreatedAtDesc();

    
    Optional<Payment> findByStripeSessionId(String stripeSessionId);
	boolean existsByOfficeIdAndPaymentMonthAndStatus(Long officeId, String paymentMonth, PaymentStatus paid);
}