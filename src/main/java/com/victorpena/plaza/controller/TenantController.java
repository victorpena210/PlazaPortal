package com.victorpena.plaza.controller;

import java.util.List;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.service.InvoiceService;
import com.victorpena.plaza.service.LeaseService;
import com.victorpena.plaza.service.MaintenanceRequestService;
import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserService userService;
    private final LeaseService leaseService;    
    private final MaintenanceRequestService maintenanceRequestService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    

    public TenantController(UserService userService, LeaseService leaseService, MaintenanceRequestService maintenanceRequestService, PaymentService paymentService, InvoiceService invoiceService) {
        this.userService = userService;
        this.leaseService = leaseService;
        this.maintenanceRequestService = maintenanceRequestService;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        
        model.addAttribute("user", user);

        
        Lease lease = leaseService.findActiveLeaseByTenantId(user.getId());

        List<Office> offices = leaseService.findOfficesByTenantId(user.getId());

        model.addAttribute("offices", offices);

        if (lease != null) {

            model.addAttribute(
                    "maintenanceRequests",
                    maintenanceRequestService.findByLeaseId(lease.getId())
            );

            model.addAttribute(
                    "payments",
                    paymentService.findByLeaseId(lease.getId())
            );
        }

        return "tenant-dashboard";
    }
    
    @GetMapping("/maintenance/new")
    public String showMaintenanceRequestForm(Authentication authentication, Model model) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        model.addAttribute("user", user);
        model.addAttribute("offices", leaseService.findOfficesByTenantId(user.getId()));

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
    	model.addAttribute("offices", leaseService.findOfficesByTenantId(user.getId()));
    	model.addAttribute("payments", paymentService.findByUserId(user.getId()));
    	
    	return "tenant-payment-form";
    	
    }
    
    @GetMapping("/payments")
    public String tenantPayments(
            Authentication authentication,
            Model model
    ) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<Office> offices = leaseService.findOfficesByTenantId(user.getId());

        model.addAttribute(
                "payments",
                paymentService.findByUserId(user.getId())
        );

        model.addAttribute(
                "offices",
                offices
        );

        return "tenant-payments";
    }
    
    @GetMapping("/invoices")
    public String invoices(Model model, Authentication authentication) {
    	
        User user =
                userService.findByEmail(authentication.getName())
                        .orElseThrow();

        model.addAttribute(
                "invoices",
                invoiceService.findByTenantId(user.getId()));
        

        return "tenant-invoices";    	
    }
    
    @GetMapping("/pay/{invoiceId}")
    public String payInvoice(@PathVariable Long invoiceId, Model model) {
    	
    	Invoice invoice =
    	        invoiceService.findById(invoiceId);    	model.addAttribute("invoice", invoice);
    	
    	        
    	model.addAttribute("invoice", invoice);
    	
    	return "payment-checkout";
    }
    
    @GetMapping("/lease")
    public String viewLease(
            Authentication authentication,
            Model model) {

        User user = userService
                .findByEmail(authentication.getName())
                .orElseThrow();

        model.addAttribute(
                "leases",
                leaseService.findLeasesByTenantId(user.getId())
        );

        return "tenant-lease";
    }
    
}
