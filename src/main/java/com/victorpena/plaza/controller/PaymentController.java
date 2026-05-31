package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.InvoiceRepository;

@Controller
public class PaymentController {
	
	private PaymentService paymentService;
	private InvoiceRepository invoiceRepository;
	
	public PaymentController(PaymentService paymentService, InvoiceRepository invoiceRepository) {
		this.paymentService = paymentService;
		this.invoiceRepository = invoiceRepository;
	}
	
	@PostMapping("/tenant/payments")
	public String submitPayment(@RequestParam Long invoiceId) {
		
		Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();
		
		paymentService.createPayment(invoice);
		
		return "redirect:/tenant/payments";
	}
	
	@PostMapping("/pay/{invoiceId}")
	public String payInvoice(@PathVariable Long invoiceId) {
		paymentService.completePayment(invoiceId);
		
		return "redirect:/tenant/invoice";
	}
	

	

}
