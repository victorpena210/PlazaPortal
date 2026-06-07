package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorpena.plaza.model.Role;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.UserRepository;
import com.victorpena.plaza.service.LeaseService;
import com.victorpena.plaza.web.LeaseForm;

@Controller
@RequestMapping("/admin/lease")
public class LeaseController {

    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final LeaseService leaseService;

    public LeaseController(
            LeaseRepository leaseRepository,
            UserRepository userRepository,
            OfficeRepository officeRepository,
            LeaseService leaseService) {

        this.leaseRepository = leaseRepository;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
        this.leaseService = leaseService;
    }

    @GetMapping
    public String listLeases(Model model) {

        model.addAttribute("leases", leaseRepository.findAll());

        return "admin-leases";
    }

    @GetMapping("/new")
    public String newLease(Model model) {

        model.addAttribute("lease", new LeaseForm());
        model.addAttribute("tenants", userRepository.findByRole(Role.TENANT));
        model.addAttribute("offices", officeRepository.findAll());

        return "lease-form";
    }

    @PostMapping("/new")
    public String createLease(
            @ModelAttribute LeaseForm form) {

        leaseService.createLease(form);

        return "redirect:/admin/lease";
    }
}