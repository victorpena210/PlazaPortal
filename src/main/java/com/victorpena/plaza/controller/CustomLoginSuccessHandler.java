package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.AccessService;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
  private final AccessService access;

  public CustomLoginSuccessHandler(AccessService access) {
    this.access = access;
  }

  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication auth)
      throws IOException {
    var session = request.getSession();
    Object next = session.getAttribute("inviteReturn");
    session.removeAttribute("inviteReturn");
    if (next instanceof String url
        && (url.startsWith("/register?token=") || url.startsWith("/join?token="))) {
      response.sendRedirect(url);
      return;
    }
    response.sendRedirect(
        access.memberships().isEmpty() ? "/tenant/dashboard" : "/admin/dashboard");
  }
}
