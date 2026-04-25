package com.victorpena.plaza.service;

import java.math.BigDecimal;
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

    public Payment createPayment(Long userId, Long officeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Office office = officeRepository.findById(officeId)
                .orElseThrow(() -> new IllegalArgumentException("Office not found: " + officeId));

        if (office.getUser() == null || !office.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only pay for your assigned office.");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOffice(office);
        payment.setAmount(office.getMonthlyRent());
        payment.setStatus(PaymentStatus.PAID);

        return paymentRepository.save(payment);
    }

    public List<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByPaidAtDesc(userId);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAllByOrderByPaidAtDesc();
    }
}