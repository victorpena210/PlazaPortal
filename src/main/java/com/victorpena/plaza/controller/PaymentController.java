package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.victorpena.plaza.service.PaymentService;

@Controller
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/tenant/payments")
    public String submitPayment(@RequestParam Long invoiceId) {

        paymentService.createPayment(invoiceId);

        return "redirect:/tenant/payments";
    }

    @PostMapping("/pay/{invoiceId}")
    public String payInvoice(@PathVariable Long invoiceId) {

        paymentService.completePayment(invoiceId);

        return "redirect:/tenant/invoice";
    }
}