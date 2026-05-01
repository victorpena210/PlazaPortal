package com.victorpena.plaza.service;

import com.victorpena.plaza.model.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.Payment;
import com.victorpena.plaza.model.PaymentStatus;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.PaymentRepository;
import com.victorpena.plaza.repository.UserRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            OfficeRepository officeRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
    }

    public Payment createPayment(Long userId, Long officeId, String paymentMonth) {
        if (paymentMonth == null || paymentMonth.isBlank()) {
            throw new IllegalArgumentException("Payment month is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Office office = officeRepository.findById(officeId)
                .orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));

        if (office.getUser() == null || !office.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only pay for your assigned office.");
        }

        if (paymentRepository.existsByOfficeIdAndPaymentMonthAndStatus(
                officeId,
                paymentMonth,
                PaymentStatus.PAID
        )) {
            throw new IllegalArgumentException("Rent for this office and month has already been paid.");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOffice(office);
        payment.setAmount(office.getMonthlyRent());
        payment.setPaymentMonth(paymentMonth);
        payment.setStatus(PaymentStatus.DUE);

        return paymentRepository.save(payment);
    }
    
    public List<Payment> findByUserId(Long userId) {
    	return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    public List<Payment> findAll() {
    	return paymentRepository.findAllByOrderByCreatedAtDesc();    }
    
    public Payment markPaymentAsPaid(String stripeSessionId) {
    	Payment payment = paymentRepository.findByStripeSessionId(stripeSessionId)
    			.orElseThrow(() -> new IllegalArgumentException("payment not found for Stripe session"));
    	payment.setStatus(PaymentStatus.PAID);
    	payment.setPaidAt(LocalDateTime.now());
    	return paymentRepository.save(payment);
    }
    
    public Payment attachStripeSession(Long paymentId, String stripeSessionId) {
    	Payment payment = paymentRepository.findById(paymentId)
    			.orElseThrow(() -> new IllegalArgumentException("payment not found: " + paymentId));
    	payment.setStripeSessionId(stripeSessionId);
    	return paymentRepository.save(payment);
    }
}