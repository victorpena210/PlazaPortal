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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
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

    @PostMapping("/tenant/payments/checkout")
    public RedirectView checkout(
            Authentication authentication,
            @RequestParam Long officeId,
            @RequestParam String paymentMonth) throws Exception {

        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        Payment payment = paymentService.createPayment(user.getId(), officeId, paymentMonth);

        Session session = stripeService.createCheckoutSession(payment);

        paymentService.attachStripeSession(payment.getId(), session.getId());

        return new RedirectView(session.getUrl());
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("session_id") String sessionId,
            RedirectAttributes redirectAttributes) {

        paymentService.markPaymentAsPaid(sessionId);
        redirectAttributes.addFlashAttribute("successMessage", "Rent paid successfully through Stripe.");

        return "redirect:/tenant/dashboard";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Stripe checkout was canceled.");
        return "redirect:/tenant/payments/new";
    }
}