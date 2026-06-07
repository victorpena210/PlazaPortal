package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.victorpena.plaza.model.Role;
import com.victorpena.plaza.model.TenantInvitation;
import com.victorpena.plaza.service.InvitationService;
import com.victorpena.plaza.service.UserService;
import com.victorpena.plaza.web.RegistrationForm;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;
    private final InvitationService invitationService;

    public RegistrationController(
            UserService userService,
            InvitationService invitationService) {

        this.userService = userService;
        this.invitationService = invitationService;
    }

    @GetMapping
    public String showRegistrationForm(
            @RequestParam String token,
            Model model) {

        TenantInvitation invitation =
                invitationService.getValidInvitation(token);

        if (invitation == null) {
            return "invalid-invite";
        }

        RegistrationForm form = new RegistrationForm();
        form.setToken(token);

        model.addAttribute("registrationForm", form);

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

        TenantInvitation invite =
                invitationService.getValidInvitation(form.getToken());

        if (invite == null) {
            return "invalid-invite";
        }

        if (userService.emailExists(invite.getEmail())) {

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
                invite.getEmail(),
                form.getPassword(),
                Role.TENANT
        );

        invitationService.markUsed(invite);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Account created successfully. Please log in.");

        return "redirect:/login";
    }
}