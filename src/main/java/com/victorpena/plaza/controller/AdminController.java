package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	private final OfficeService officeService;
	private final UserService userService;

	
	public AdminController(OfficeService officeService, UserService userService) {
		this.officeService = officeService;
		this.userService = userService;
	}
	
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("offices", officeService.findAll());
		model.addAttribute("tenants", userService.findTenants());
	    model.addAttribute("availableOffices", officeService.findAvailableOffices());
		model.addAttribute("availableOfficeCount", officeService.countAvailableOffices());
		model.addAttribute("occupiedOfficeCount", officeService.countOccupiedOffices());
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
	
}
