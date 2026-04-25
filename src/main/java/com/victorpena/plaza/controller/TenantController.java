package com.victorpena.plaza.controller;

import java.util.List;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.service.MaintenanceRequestService;
import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserService userService;
    private final OfficeService officeService;
    private final MaintenanceRequestService maintenanceRequestService;
    private final PaymentService paymentService;

    public TenantController(UserService userService, OfficeService officeService, MaintenanceRequestService maintenanceRequestService, PaymentService paymentService) {
        this.userService = userService;
        this.officeService = officeService;
        this.maintenanceRequestService = maintenanceRequestService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        List<Office> offices = officeService.findByUserId(user.getId());

        model.addAttribute("requests", maintenanceRequestService.findByUserId(user.getId()));
        model.addAttribute("user", user);
        model.addAttribute("offices", offices);

        return "tenant-dashboard";
    }
    
    @GetMapping("/maintenance/new")
    public String showMaintenanceRequestForm(Authentication authentication, Model model) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        model.addAttribute("user", user);
        model.addAttribute("offices", officeService.findByUserId(user.getId()));

        return "tenant-maintenance-form";
    }
    
    @PostMapping("/maintenance")
    public String submitMaintenanceRequest(
            Authentication authentication,
            @RequestParam Long officeId,
            @RequestParam String title,
            @RequestParam String description,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        try {
            maintenanceRequestService.createRequest(user.getId(), officeId, title, description);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance request submitted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/tenant/dashboard";
    }
    
    @GetMapping("/payments/new")
    public String showPaymentForm(Authentication authentication, Model model) {
    	String email = authentication.getName();
    	User user = userService.findByEmail(email)
    			.orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    	
    	model.addAttribute("user", user);
    	model.addAttribute("offices", officeService.findByUserId(user.getId()));
    	model.addAttribute("payments", paymentService.findByUserId(user.getId()));
    	
    	return "tenant-payment-form";
    	
    }
    
    @PostMapping("/payments")
    public String submitPayment(Authentication authentication, @RequestParam Long officeId, RedirectAttributes redirectAttributes) {
    	String email = authentication.getName();
    	
    	User user = userService.findByEmail(email)
    			.orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    	
    	try {
    		paymentService.createPayment(user.getId(), officeId);
    		redirectAttributes.addFlashAttribute("successMessage", "Rent payment submitted successfully.");
    	} catch (IllegalArgumentException e) {
    		redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    	}
    	return "redirect:/tenant/dashboard";
    }
}