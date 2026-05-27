package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

import com.victorpena.plaza.model.Payment;
import com.victorpena.plaza.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Payment> findAllByOrderByCreatedAtDesc();
    
	boolean existsByOfficeIdAndPaymentMonthAndStatus(Long officeId, String paymentMonth, PaymentStatus paid);
	
	Optional<Payment> findByUserAndPaymentMonthAndStatus(User user, String paymentMonth, PaymentStatus status);

	boolean existsByOfficeIdAndPaymentMonthAndStatusIn(Long officeId, String paymentMonth, List<PaymentStatus> of);
}