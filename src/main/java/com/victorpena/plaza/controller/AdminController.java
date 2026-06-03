package com.victorpena.plaza.controller;

import com.victorpena.plaza.model.MaintenanceRequestStatus;
import com.victorpena.plaza.model.TenantInvitation;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.TenantInvitationRepository;
import com.victorpena.plaza.repository.UserRepository;
import com.victorpena.plaza.service.MaintenanceRequestService;
import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OfficeService officeService;
    private final UserService userService;
    private final MaintenanceRequestService maintenanceRequestService;
    private final PaymentService paymentService;
    private final LeaseRepository leaseRepository;
    private final TenantInvitationRepository invitationRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public AdminController(
    		UserRepository userRepository,
            OfficeService officeService,
            UserService userService,
            MaintenanceRequestService maintenanceRequestService,
            PaymentService paymentService,
            LeaseRepository leaseRepository, TenantInvitationRepository invitationRepository, JavaMailSender mailSender) {

        this.officeService = officeService;
        this.userService = userService;
        this.maintenanceRequestService = maintenanceRequestService;
        this.paymentService = paymentService;
        this.leaseRepository = leaseRepository;
        this.invitationRepository = invitationRepository;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    /*
     * =========================================
     * ADMIN DASHBOARD
     * =========================================
     */

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("offices", officeService.findAll());

        model.addAttribute("tenants", userService.findTenants());

        model.addAttribute(
                "availableOffices",
                officeService.findAvailableOffices());

        model.addAttribute(
                "availableOfficeCount",
                officeService.countAvailableOffices());

        model.addAttribute(
                "occupiedOfficeCount",
                officeService.countOccupiedOffices());

        model.addAttribute(
                "maintenanceOffices",
                officeService.findMaintenanceOffices());

        model.addAttribute(
                "maintenanceOfficeCount",
                officeService.countMaintenanceOffices());

        model.addAttribute(
                "totalOfficeCount",
                officeService.countAllOffices());

        // ACTIVE NAVBAR PAGE
        model.addAttribute("activePage", "admin-dashboard");

        return "admin-dashboard";
    }

    /*
     * =========================================
     * ASSIGN OFFICE
     * =========================================
     */

    @PostMapping("/offices/assign")
    public String assignOffice(
            @RequestParam Long officeId,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes) {

        try {

            officeService.assignOfficeToTenant(officeId, userId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Office assigned successfully.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /*
     * =========================================
     * UNASSIGN OFFICE
     * =========================================
     */

    @PostMapping("/offices/{officeId}/unassign")
    public String unassignOffice(
            @PathVariable Long officeId,
            RedirectAttributes redirectAttributes) {

        try {

            officeService.unassignOffice(officeId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Office unassigned successfully.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /*
     * =========================================
     * MAINTENANCE REQUESTS PAGE
     * =========================================
     */

    @GetMapping("/maintenance")
    public String maintenanceRequests(Model model) {

        model.addAttribute(
                "requests",
                maintenanceRequestService.findAll());

        // ACTIVE NAVBAR PAGE
        model.addAttribute("activePage", "maintenance");

        return "admin-maintenance-requests";
    }

    /*
     * =========================================
     * UPDATE MAINTENANCE STATUS
     * =========================================
     */

    @PostMapping("/maintenance/{requestId}/status")
    public String updateMaintenanceStatus(
            @PathVariable Long requestId,
            @RequestParam MaintenanceRequestStatus status,
            RedirectAttributes redirectAttributes) {

        try {

            maintenanceRequestService.updateStatus(requestId, status);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Maintenance request updated successfully.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/maintenance";
    }

    /*
     * =========================================
     * MARK OFFICE AS MAINTENANCE
     * =========================================
     */

    @PostMapping("/offices/{officeId}/maintenance")
    public String markOfficeAsMaintenance(
            @PathVariable Long officeId,
            RedirectAttributes redirectAttributes) {

        try {

            officeService.markOfficeAsMaintenance(officeId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Office marked as maintenance.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /*
     * =========================================
     * MARK OFFICE AS AVAILABLE
     * =========================================
     */

    @PostMapping("/offices/{officeId}/available")
    public String markOfficeAsAvailable(
            @PathVariable Long officeId,
            RedirectAttributes redirectAttributes) {

        try {

            officeService.markOfficeAsAvailable(officeId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Office marked as available.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
    
    
    /*
     * =========================================
     * LEASES PAGE
     * =========================================
     */
    
    @GetMapping("/leases")
    public String leases(Model model) {
    	
    	model.addAttribute("leases", leaseRepository.findAll());
    	
    	model.addAttribute("activePage", "leases");
    	
    	return "admin-leases";
    }

    
    

    /*
     * =========================================
     * PAYMENTS PAGE
     * =========================================
     */

    @GetMapping("/payments")
    public String payments(Model model) {

        model.addAttribute(
                "payments",
                paymentService.findAll());

        // ACTIVE NAVBAR PAGE
        model.addAttribute("activePage", "payments");

        return "admin-payments";
    }
    
    /*
     * =========================================
     * tenant invitation
     * =========================================
     */
    
    @PostMapping("/invite")
    public String inviteTenant(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        try {
        	
        	if (userRepository.existsByEmail(email)) {

        	    redirectAttributes.addFlashAttribute(
        	            "errorMessage",
        	            "A user with this email already exists.");

        	    return "redirect:/admin/tenants";
        	}
        	
        	Optional<TenantInvitation> existingInvite =
        	        invitationRepository.findByEmailAndUsedFalse(email);

        	if (existingInvite.isPresent()
        	        && existingInvite.get().getExpiresAt()
        	                .isAfter(LocalDateTime.now())) {

        	    redirectAttributes.addFlashAttribute(
        	            "errorMessage",
        	            "An active invitation has already been sent to this email.");

        	    return "redirect:/admin/tenants";
        	}
            // Create invitation
            TenantInvitation invitation = new TenantInvitation();

            invitation.setEmail(email);
            invitation.setToken(UUID.randomUUID().toString());
            invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
            invitation.setUsed(false);

            invitationRepository.save(invitation);

            // Registration link
            String registrationLink =
                    "http://localhost:8080/register?token="
                            + invitation.getToken();

            // Email
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom("13victor.pena@gmail.com");
            message.setTo(email);

            message.setSubject(
                    "Peña Plaza Tenant Registration");

            message.setText(
                    "You have been invited to register for Peña Plaza.\n\n"
                            + "Click the link below to create your account:\n\n"
                            + registrationLink);

            // Send email
            mailSender.send(message);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Invitation email sent to " + email);

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to send email. Gmail SMTP connection failed.");

        }

        return "redirect:/admin/tenants";
    }
    
    @GetMapping("/tenants")
    public String manageTenants(Model model) {

        model.addAttribute("tenants", userService.findTenants());

        model.addAttribute("activePage", "tenants");

        return "manage-tenants";
    }
}