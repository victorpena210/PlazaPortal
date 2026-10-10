package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.*;
import jakarta.servlet.http.*;
import java.util.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ViewAdvice {
  private final AccessService access;
  private final com.victorpena.plaza.repository.MaintenancePhotoRepository photos;

  public ViewAdvice(
      AccessService access, com.victorpena.plaza.repository.MaintenancePhotoRepository photos) {
    this.access = access;
    this.photos = photos;
  }

  @ModelAttribute
  public void common(Model model, HttpServletRequest request) {
    var a =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    if (a == null
        || a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken
        || !a.isAuthenticated()) return;
    var user = access.user();
    model.addAttribute("currentUser", user);
    model.addAttribute("photos", photos);
    var memberships = access.memberships();
    model.addAttribute("memberships", memberships);
    if (!memberships.isEmpty()) {
      var id = (Long) request.getSession().getAttribute("companyId");
      var selected =
          memberships.stream()
              .filter(m -> m.getCompany().getId().equals(id))
              .findFirst()
              .orElse(memberships.get(0));
      model.addAttribute("selectedCompany", selected.getCompany());
      model.addAttribute(
          "companyOwner", selected.getRole() == com.victorpena.plaza.model.MembershipRole.OWNER);
    }
  }
}
