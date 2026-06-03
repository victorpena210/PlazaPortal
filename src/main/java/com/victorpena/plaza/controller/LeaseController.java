package com.victorpena.plaza.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.Role;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.InvoiceRepository;
import com.victorpena.plaza.repository.LeaseRepository;
import com.victorpena.plaza.repository.OfficeRepository;
import com.victorpena.plaza.repository.UserRepository;
import com.victorpena.plaza.web.LeaseForm;

@Controller
@RequestMapping("/admin/lease")
public class LeaseController {
	
    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final InvoiceRepository invoiceRepository;

    public LeaseController(
            LeaseRepository leaseRepository, UserRepository userRepository, OfficeRepository officeRepository, InvoiceRepository invoiceRepository) {
    	this.officeRepository = officeRepository;
        this.leaseRepository = leaseRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
    }
    
    @GetMapping
    public String listLeases(Model model) {
    	
    	model.addAttribute("leases", leaseRepository.findAll());
    	
    	return "lease-list";
    	
    }
    
    @GetMapping("/new")
    public String newLease(Model model) {
    	
    	model.addAttribute("lease", new Lease());
    	
    	model.addAttribute("tenants", userRepository.findByRole(Role.TENANT));
    	
    	model.addAttribute("offices", officeRepository.findAll());
    	
    	return "lease-form";
    }
    
    @PostMapping("/new")
    public String createLease(
            @ModelAttribute LeaseForm form) {

        User tenant =
                userRepository.findById(form.getTenantId())
                        .orElseThrow();

        Office office =
                officeRepository.findById(form.getOfficeId())
                        .orElseThrow();

        Lease lease = new Lease();

        lease.setTenant(tenant);
        lease.setOffice(office);

        lease.setMonthlyRent(form.getMonthlyRent());
        lease.setStartDate(form.getStartDate());
        lease.setEndDate(form.getEndDate());

        lease.setActive(true);

        lease = leaseRepository.save(lease);
        Invoice invoice = new Invoice();

        invoice.setLease(lease);

        invoice.setAmount(
                lease.getMonthlyRent());

        invoice.setDueDate(
                lease.getStartDate()
                     .withDayOfMonth(1));

        invoice.setStatus(
                InvoiceStatus.PENDING);

        invoice.setCreatedAt(
                LocalDateTime.now());

        invoiceRepository.save(invoice);
        return "redirect:/admin/lease";
    }
}
