package com.victorpena.plaza.controller;

import com.victorpena.plaza.model.MaintenanceRequestStatus;
import com.victorpena.plaza.repository.InvoiceRepository;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.service.MaintenanceRequestService;
import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OfficeService officeService;
    private final UserService userService;
    private final MaintenanceRequestService maintenanceRequestService;
    private final PaymentService paymentService;
    private final LeaseRepository leaseRepository;
    private final InvoiceRepository invoiceRepository;

    public AdminController(
            OfficeService officeService,
            UserService userService,
            MaintenanceRequestService maintenanceRequestService,
            PaymentService paymentService,
            LeaseRepository leaseRepository, InvoiceRepository invoiceRepository) {

        this.officeService = officeService;
        this.userService = userService;
        this.maintenanceRequestService = maintenanceRequestService;
        this.paymentService = paymentService;
        this.leaseRepository = leaseRepository;
        this.invoiceRepository = invoiceRepository;
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

}