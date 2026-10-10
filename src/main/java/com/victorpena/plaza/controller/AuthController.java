package com.victorpena.plaza.controller;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.service.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
  private final CompanyService companies;
  private final InvitationService invites;
  private final UserService users;
  private final AccessService access;
  private final PasswordResetService resets;
  private final boolean signup;

  public AuthController(
      CompanyService companies,
      InvitationService invites,
      UserService users,
      AccessService access,
      PasswordResetService resets,
      @Value("${app.owner-signup-enabled:false}") boolean signup) {
    this.companies = companies;
    this.invites = invites;
    this.users = users;
    this.access = access;
    this.resets = resets;
    this.signup = signup;
  }

  @GetMapping("/")
  public String home(Model m) {
    m.addAttribute("signupEnabled", signup);
    return "home";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/signup")
  public String signup() {
    if (!signup)
      throw new org.springframework.security.access.AccessDeniedException(
          "Owner signup is disabled.");
    return "signup";
  }

  @PostMapping("/signup")
  public String signup(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String email,
      @RequestParam String password,
      @RequestParam String companyName,
      RedirectAttributes flash) {
    if (!signup)
      throw new org.springframework.security.access.AccessDeniedException(
          "Owner signup is disabled.");
    companies.signup(firstName, lastName, email, password, companyName);
    flash.addFlashAttribute("successMessage", "Company account created. Sign in to continue.");
    return "redirect:/login";
  }

  private User current() {
    var a =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    return a == null
            || a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken
        ? null
        : access.user();
  }

  @GetMapping({"/register", "/join"})
  public String invite(@RequestParam String token, HttpServletRequest r, Model m) {
    boolean member = r.getRequestURI().equals("/join");
    String email;
    String company;
    if (member) {
      var i = companies.valid(token);
      email = i.getEmail();
      company = i.getCompany().getName();
    } else {
      var i = invites.valid(token);
      email = i.getLease().getTenantEmail();
      company = i.getLease().getOffice().getProperty().getCompany().getName();
    }
    r.getSession()
        .setAttribute(
            "inviteReturn",
            (member ? "/join" : "/register")
                + "?token="
                + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8));
    m.addAttribute("token", token);
    m.addAttribute("invitedEmail", email);
    m.addAttribute("companyName", company);
    m.addAttribute("accountExists", users.emailExists(email));
    m.addAttribute("invitePath", member ? "/join" : "/register");
    return "register";
  }

  @PostMapping({"/register", "/join"})
  public String accept(
      @RequestParam String token,
      @RequestParam(defaultValue = "") String firstName,
      @RequestParam(defaultValue = "") String lastName,
      @RequestParam(defaultValue = "") String password,
      HttpServletRequest r,
      RedirectAttributes flash) {
    if (r.getRequestURI().equals("/join"))
      companies.accept(token, firstName, lastName, password, current());
    else invites.accept(token, firstName, lastName, password, current());
    r.getSession().removeAttribute("inviteReturn");
    flash.addFlashAttribute(
        "successMessage", "Invitation accepted. Sign in or open your account to continue.");
    return "redirect:/login";
  }

  @GetMapping("/forgot-password")
  public String forgot() {
    return "forgot-password";
  }

  @PostMapping("/forgot-password")
  public String forgot(@RequestParam String email, RedirectAttributes flash) {
    resets.request(email);
    flash.addFlashAttribute(
        "successMessage", "If an enabled account matches that email, a reset link will be sent.");
    return "redirect:/forgot-password";
  }

  @GetMapping("/reset-password")
  public String reset(@RequestParam String token, Model m) {
    m.addAttribute("token", token);
    return "reset-password";
  }

  @PostMapping("/reset-password")
  public String reset(
      @RequestParam String token,
      @RequestParam String password,
      HttpServletRequest r,
      RedirectAttributes flash) {
    resets.reset(token, password);
    r.getSession().invalidate();
    flash.addFlashAttribute("successMessage", "Password updated. Sign in with your new password.");
    return "redirect:/login";
  }
}
