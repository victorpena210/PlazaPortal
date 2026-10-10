package com.victorpena.plaza.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import com.victorpena.plaza.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

@RestController
public class StripeWebhookController {
  private final StripeGateway stripe;
  private final PaymentTransactions transactions;
  private final String secret;

  public StripeWebhookController(
      StripeGateway stripe,
      PaymentTransactions transactions,
      @Value("${stripe.webhook-secret:}") String secret) {
    this.stripe = stripe;
    this.transactions = transactions;
    this.secret = secret;
  }

  @PostMapping("/webhooks/stripe")
  public ResponseEntity<String> webhook(
      @RequestBody String payload,
      @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
    if (secret.isBlank()) return ResponseEntity.status(503).body("Webhook not configured");
    if (signature == null || payload.length() > 1000000)
      return ResponseEntity.badRequest().body("Invalid webhook");
    com.stripe.model.Event event;
    try {
      event = Webhook.constructEvent(payload, signature, secret);
    } catch (SignatureVerificationException | RuntimeException e) {
      return ResponseEntity.badRequest().body("Invalid signature or payload");
    }
    String type = event.getType();
    String account = event.getAccount() == null ? "platform" : event.getAccount();
    var object = JsonMapper.builder().build().readTree(payload).path("data").path("object");
    try {
      if (java.util.Set.of(
              "checkout.session.completed",
              "checkout.session.async_payment_succeeded",
              "checkout.session.async_payment_failed",
              "checkout.session.expired")
          .contains(type)) {
        var s = stripe.retrieve(object.path("id").asText(), account);
        transactions.apply(event.getId(), s, account, type);
        if ("paid".equals(s.getPaymentStatus())) {
          var charge = stripe.latestCharge(s.getPaymentIntent(), account);
          if (charge != null
              && charge.getAmountRefunded() != null
              && charge.getAmountRefunded() > 0)
            transactions.refund(
                "refund-sync-" + charge.getId() + "-" + charge.getAmountRefunded(),
                charge,
                account);
        }
      } else if (type.equals("charge.refunded")) {
        transactions.refund(
            event.getId(), stripe.charge(object.path("id").asText(), account), account);
      }
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("Payment verification mismatch");
    }
    return ResponseEntity.ok("received");
  }
}
