package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
		model.addAttribute("availableOfficeCount", officeService.countAvailableOffices());
		model.addAttribute("occupiedOfficeCount", officeService.countOccupiedOffices());
		model.addAttribute("totalOfficeCount", officeService.countAllOffices());
		
		return "admin-dashboard";
	}
}
