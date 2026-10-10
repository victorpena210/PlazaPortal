package com.victorpena.plaza.controller;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CompanyController {
  private final AccessService access;
  private final CompanyService companies;
  private final CompanyMembershipRepository members;
  private final AuditEntryRepository audit;

  public CompanyController(
      AccessService access,
      CompanyService companies,
      CompanyMembershipRepository members,
      AuditEntryRepository audit) {
    this.access = access;
    this.companies = companies;
    this.members = members;
    this.audit = audit;
  }

  @PostMapping("/companies/switch")
  public String select(@RequestParam Long companyId, HttpSession s) {
    access.member(companyId);
    s.setAttribute("companyId", companyId);
    return "redirect:/admin/dashboard";
  }

  @GetMapping("/companies/new")
  public String newCompany() {
    access.user();
    return "company-new";
  }

  @PostMapping("/companies/new")
  public String create(@RequestParam String name, HttpSession s) {
    var c = companies.create(access.user(), name);
    s.setAttribute("companyId", c.getId());
    return "redirect:/admin/dashboard";
  }

  @GetMapping("/admin/company")
  public String company(HttpSession s, Model m) {
    var c = access.company(s);
    access.owner(c.getId());
    m.addAttribute("company", c);
    m.addAttribute("team", members.findByCompanyIdOrderById(c.getId()));
    m.addAttribute("auditEntries", audit.findTop100ByCompanyIdOrderByCreatedAtDesc(c.getId()));
    return "company-settings";
  }

  @PostMapping("/admin/company")
  public String update(
      HttpSession s, @RequestParam String name, @RequestParam String supportEmail) {
    companies.update(access.company(s), name, supportEmail);
    return "redirect:/admin/company";
  }

  @PostMapping("/admin/company/invite")
  public String invite(
      HttpSession s,
      @RequestParam String email,
      @RequestParam MembershipRole role,
      RedirectAttributes f) {
    companies.invite(access.company(s), email, role);
    f.addFlashAttribute("successMessage", "Invitation queued for delivery.");
    return "redirect:/admin/company";
  }

  @PostMapping("/admin/company/members/{id}/remove")
  public String remove(HttpSession s, @PathVariable Long id) {
    companies.remove(access.company(s), id);
    return "redirect:/admin/company";
  }
}
