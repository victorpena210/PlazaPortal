package com.victorpena.plaza.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.victorpena.plaza.service.PaymentService;
import com.stripe.model.checkout.Session;
@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    private final PaymentService paymentService;
    
    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );

        } catch (SignatureVerificationException e) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid signature");
        }

        System.out.println("Webhook verified!");

        if ("checkout.session.completed".equals(event.getType())) {

            System.out.println("Checkout completed!");

            Session session = (Session) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            paymentService.markPaymentAsPaid(session.getId());
        }

        return ResponseEntity.ok("Success");
    }
}