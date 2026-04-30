package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByUserIdOrderByPaidAtDesc(Long userId);
    List<Payment> findAllByOrderByPaidAtDesc();

    boolean existsByOfficeIdAndPaymentMonth(Long officeId, String paymentMonth);
    
    Optional<Payment> findByStripeSessionId(String stripeSessionId);
}