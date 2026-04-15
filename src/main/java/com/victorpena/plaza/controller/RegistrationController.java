package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.victorpena.plaza.model.Role;
import com.victorpena.plaza.service.UserService;
import com.victorpena.plaza.web.RegistrationForm;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping
    public String registerUser(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Passwords do not match.");
        }

        if (userService.emailExists(form.getEmail())) {
            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "An account with this email already exists.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.registerUser(
                form.getFirstName(),
                form.getLastName(),
                form.getEmail(),
                form.getPassword(),
                Role.TENANT
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Account created successfully. You can log in once login is enabled.");
        return "redirect:/register";
    }
}