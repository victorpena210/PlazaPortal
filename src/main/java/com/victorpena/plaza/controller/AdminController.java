package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.victorpena.plaza.model.MaintenanceRequestStatus;
import com.victorpena.plaza.service.MaintenanceRequestService;
import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.service.PaymentService;
import com.victorpena.plaza.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	private final OfficeService officeService;
	private final UserService userService;
	private final MaintenanceRequestService maintenanceRequestService;
	private final PaymentService paymentService;

	
	public AdminController(OfficeService officeService, UserService userService, MaintenanceRequestService maintenanceRequestService, PaymentService paymentService) {
		this.officeService = officeService;
		this.userService = userService;
		this.maintenanceRequestService = maintenanceRequestService;
		this.paymentService = paymentService;
	}
	
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("offices", officeService.findAll());
		model.addAttribute("tenants", userService.findTenants());
	    model.addAttribute("availableOffices", officeService.findAvailableOffices());
		model.addAttribute("availableOfficeCount", officeService.countAvailableOffices());
		model.addAttribute("occupiedOfficeCount", officeService.countOccupiedOffices());
		model.addAttribute("maintenanceOffices", officeService.findMaintenanceOffices());
	    model.addAttribute("maintenanceOfficeCount", officeService.countMaintenanceOffices());
		model.addAttribute("totalOfficeCount", officeService.countAllOffices());
		
		return "admin-dashboard";
	}
	
	@PostMapping("/offices/assign")
	public String assignOffice(
			@RequestParam Long officeId,
			@RequestParam long userId,
			RedirectAttributes redirectAttributes) {
		try {
			officeService.assignOfficeToTenant(officeId, userId);
			redirectAttributes.addFlashAttribute("successMessage", "Office assigned successfully.");
			
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/admin/dashboard";
			}
	
	@PostMapping("/offices/{officeId}/unassign")
	public String unassignOffice(
			@PathVariable Long officeId,
			RedirectAttributes redirectAttributes) {
		try {
			officeService.unassignOffice(officeId);
			redirectAttributes.addFlashAttribute("successMessage", "Office unassigned successfully.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/admin/dashboard";
	}
	
	@GetMapping("/maintenance")
	public String maintenanceRequests(Model model) {
		model.addAttribute("requests", maintenanceRequestService.findAll());
		return "admin-maintenance-requests";
	}
	
	@PostMapping("/maintenance/{requestId}/status")
	public String updateMaintenanceStatus(
			@PathVariable Long requestId,
			@RequestParam MaintenanceRequestStatus status,
			RedirectAttributes redirectAttributes) {
		try {
			maintenanceRequestService.updateStatus(requestId, status);
			redirectAttributes.addFlashAttribute("successMessage", "Maintenance request updated successfuly.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		
		return "redirect:/admin/maintenance";
	}
	
	@PostMapping("/offices/{officeId}/maintenance")
	public String markOfficeAsMaintenance(
			@PathVariable Long officeId, RedirectAttributes redirectAttributes) {
		try {
			officeService.markOfficeAsMaintenance(officeId);
			redirectAttributes.addFlashAttribute("successMessage", "Office marked as maintenace.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		
		return "redirect:/admin/dashboard";
	}
	
	@PostMapping("/offices/{officeId}/available")
	public String markOfficeAsAvailable(
	        @PathVariable Long officeId,
	        RedirectAttributes redirectAttributes) {
	    try {
	        officeService.markOfficeAsAvailable(officeId);
	        redirectAttributes.addFlashAttribute("successMessage", "Office marked as available.");
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
	    }

	    return "redirect:/admin/dashboard";
	}
	
	@GetMapping("/payments")
	public String payments(Model model) {
	    model.addAttribute("payments", paymentService.findAll());
	    return "admin-payments";
	}
	
	
}
