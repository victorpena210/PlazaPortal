package com.victorpena.plaza.service;

import com.victorpena.plaza.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.Payment;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.PaymentRepository;
import com.victorpena.plaza.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
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

        boolean existingPayment =
        	    paymentRepository.existsByOfficeIdAndPaymentMonthAndStatusIn(
        	        officeId,
        	        paymentMonth,
        	        List.of(
        	            PaymentStatus.PENDING,
        	            PaymentStatus.PAID
        	        )
        	    );

        	if(existingPayment) {
        	    throw new IllegalArgumentException(
        	        "A payment already exists for this month."
        	    );
        	}

        Payment payment = new Payment();
        
        BigDecimal rent = office.getMonthlyRent();
        BigDecimal lateFee = BigDecimal.ZERO;

        if (LocalDate.now().getDayOfMonth() > 5) {
            lateFee = new BigDecimal("75.00");
        }

        payment.setUser(user);
        payment.setOffice(office);
        payment.setAmount(rent);
        payment.setLateFee(lateFee);
        payment.setTotalAmount(rent.add(lateFee));       
        payment.setPaymentMonth(paymentMonth);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
    
    public List<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional
    public Payment markAsPaid(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId
                        ));

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
    
}