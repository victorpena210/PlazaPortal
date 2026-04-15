package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tenant")
public class TenantController {
	
	@GetMapping("/dashboard")
	public String dashboard() {
		return "tenant-dashboard";
	}

}
