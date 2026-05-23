package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.OfficeService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final OfficeService officeService;

    public HomeController(OfficeService officeService) {

        this.officeService = officeService;
    }

    /*
     * =========================================
     * HOME PAGE
     * =========================================
     */

    @GetMapping("/")
    public String home(Model model) {

        // OFFICE LIST
        model.addAttribute(
                "offices",
                officeService.findAll());

        // ACTIVE NAVBAR PAGE
        model.addAttribute(
                "activePage",
                "home");

        return "home";
    }

}