package com.victorpena.plaza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /*
     * =========================================
     * LOGIN PAGE
     * =========================================
     */

    @GetMapping("/login")
    public String login(Model model) {

        // ACTIVE NAVBAR PAGE
        model.addAttribute(
                "activePage",
                "login");

        return "login";
    }

}