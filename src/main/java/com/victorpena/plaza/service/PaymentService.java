package com.victorpena.plaza.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Payment;
import com.victorpena.plaza.model.PaymentStatus;
import com.victorpena.plaza.repository.InvoiceRepository;
import com.victorpena.plaza.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository) {

        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
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

    @Transactional
    public Payment createPayment(Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow();

        return createPayment(invoice);
    }

    @Transactional
    private Payment createPayment(Invoice invoice) {

        Payment payment = new Payment();

        payment.setInvoice(invoice);
        payment.setUser(invoice.getLease().getPortalAccess());
        payment.setOffice(invoice.getLease().getOffice());
        payment.setAmount(invoice.getAmount());
        payment.setTotalAmount(invoice.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void completePayment(Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow();

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice already paid");
        }

        Payment payment =
                paymentRepository.findByInvoiceId(invoiceId)
                        .orElseThrow();

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        invoice.setStatus(InvoiceStatus.PAID);

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
    }
}