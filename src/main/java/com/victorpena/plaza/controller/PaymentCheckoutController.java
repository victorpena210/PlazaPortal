package com.victorpena.plaza.controller;

import com.stripe.model.checkout.Session;

import com.victorpena.plaza.model.Payment;
import com.victorpena.plaza.model.User;

import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.StripeService;
import com.victorpena.plaza.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping
public class PaymentCheckoutController {

    private final PaymentService paymentService;
    private final StripeService stripeService;
    private final UserService userService;

    public PaymentCheckoutController(
            PaymentService paymentService,
            StripeService stripeService,
            UserService userService) {

        this.paymentService = paymentService;
        this.stripeService = stripeService;
        this.userService = userService;
    }

    /*
     * =========================================
     * STRIPE CHECKOUT
     * =========================================
     */

    @PostMapping("/tenant/payments/checkout")
    public RedirectView checkout(
            Authentication authentication,
            @RequestParam Long officeId,
            @RequestParam String paymentMonth)
            throws Exception {

        // CURRENT LOGGED-IN USER EMAIL
        String email = authentication.getName();

        // FIND USER
        User user = userService.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + email));

        // CREATE PAYMENT RECORD
        Payment payment = paymentService.createPayment(
                user.getId(),
                officeId,
                paymentMonth);

        // CREATE STRIPE SESSION
        Session session = stripeService.createCheckoutSession(payment);

        // SAVE STRIPE SESSION ID
        paymentService.attachStripeSession(
                payment.getId(),
                session.getId());

        // REDIRECT TO STRIPE CHECKOUT
        return new RedirectView(session.getUrl());
    }

    /*
     * =========================================
     * PAYMENT SUCCESS
     * =========================================
     */

    @GetMapping("/payment/success")
    public String paymentSuccess(
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Stripe payment submitted successfully.");

        return "redirect:/tenant/dashboard";
    }

    /*
     * =========================================
     * PAYMENT CANCELED
     * =========================================
     */

    @GetMapping("/payment/cancel")
    public String paymentCancel(
            @RequestParam("session_id") String sessionId,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Stripe checkout was canceled.");

        return "redirect:/tenant/payments/new";
    }

}