package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorpena.plaza.repository.LeaseRepository;

@Controller
@RequestMapping("/admin/lease")
public class LeaseController {
	
    private final LeaseRepository leaseRepository;

    public LeaseController(
            LeaseRepository leaseRepository) {

        this.leaseRepository = leaseRepository;
    }
    
    @GetMapping
    public String listLeases(Model model) {
    	
    	model.addAttribute("leases", leaseRepository.findAll());
    	
    	return "lease-list";
    	
    }

}
