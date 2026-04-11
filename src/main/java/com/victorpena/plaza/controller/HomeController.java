package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.victorpena.plaza.service.OfficeService;

@Controller
public class HomeController {
	
	private final OfficeService officeService;
	
	public HomeController(OfficeService officeService) {
		this.officeService = officeService;
	}
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("offices", officeService.findAll());
		return "home";
	}

}
