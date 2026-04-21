package com.victorpena.plaza.controller;

import java.util.List;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.service.OfficeService;
import com.victorpena.plaza.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserService userService;
    private final OfficeService officeService;

    public TenantController(UserService userService, OfficeService officeService) {
        this.userService = userService;
        this.officeService = officeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        List<Office> offices = officeService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("offices", offices);

        return "tenant-dashboard";
    }
}