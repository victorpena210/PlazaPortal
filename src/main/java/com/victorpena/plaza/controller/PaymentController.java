package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.victorpena.plaza.model.User;

@Controller
public class PaymentController {
	
	private UserService userService;
	private PaymentService paymentService;
	
	public PaymentController(UserService userService, PaymentService paymentService) {
		this.userService = userService;
		this.paymentService = paymentService;
	}
	
	@PostMapping("/tenant/payments/create")
	public String createPayment(
			@RequestParam Long officeId,
			@RequestParam String paymentMonth,
			Authentication authentication,
			RedirectAttributes redirectAttributes
			) {
		User user = userService.findByEmail(authentication.getName())
		        .orElseThrow(() -> new IllegalArgumentException("User not found"));
		
		paymentService.createPayment(
				user.getId(),
				officeId,
				paymentMonth);
		
		redirectAttributes.addFlashAttribute(
				"success",
				"Payment request submitted successfully."
				);
		
		return "redirect:/tenant/payments";
	}

}
