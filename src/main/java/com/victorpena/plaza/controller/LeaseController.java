package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorpena.plaza.service.LeaseService;
import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.web.LeaseForm;

@Controller
@RequestMapping("/admin/lease")
public class LeaseController {

    private final LeaseService leaseService;
    private final OfficeService officeService;

    public LeaseController(
    		OfficeService officeService,
            LeaseService leaseService) {

        this.officeService = officeService;
        this.leaseService = leaseService;
    }

    @GetMapping
    public String listLeases(Model model) {

        model.addAttribute("leases", leaseService.findAll());

        return "admin-leases";
    }

    @GetMapping("/new")
    public String newLease(Model model) {

        model.addAttribute("lease", new LeaseForm());
        model.addAttribute("offices", officeService.findAll());

        return "lease-form";
    }

    @PostMapping("/new")
    public String createLease(
            @ModelAttribute LeaseForm form) {

        leaseService.createLease(form);

        return "redirect:/admin/lease";
    }
}