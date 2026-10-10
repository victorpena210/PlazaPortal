package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessService {
  private final UserRepository users;
  private final CompanyMembershipRepository memberships;

  public AccessService(UserRepository users, CompanyMembershipRepository memberships) {
    this.users = users;
    this.memberships = memberships;
  }

  public User user() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null
        || a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken
        || !a.isAuthenticated()) throw new AccessDeniedException("Sign in required.");
    return users
        .findByEmail(a.getName().toLowerCase(java.util.Locale.ROOT))
        .filter(User::isEnabled)
        .orElseThrow(() -> new AccessDeniedException("Account unavailable."));
  }

  public List<CompanyMembership> memberships() {
    return memberships.findByUserIdOrderById(user().getId());
  }

  public Company company(HttpSession session) {
    var list = memberships();
    Long id = (Long) session.getAttribute("companyId");
    if (id == null && !list.isEmpty()) {
      id = list.get(0).getCompany().getId();
      session.setAttribute("companyId", id);
    }
    if (id == null) throw new AccessDeniedException("Join or create a company first.");
    return member(id).getCompany();
  }

  public CompanyMembership member(Long companyId) {
    return memberships
        .findByCompanyIdAndUserId(companyId, user().getId())
        .orElseThrow(() -> new AccessDeniedException("Company access denied."));
  }

  public void owner(Long companyId) {
    if (member(companyId).getRole() != MembershipRole.OWNER)
      throw new AccessDeniedException("Only a company owner can do this.");
  }

  public void sameCompany(Long companyId, Company actual) {
    member(companyId);
    if (!companyId.equals(actual.getId())) throw missing();
  }

  public void tenant(Lease lease) {
    if (lease.getPortalAccess() == null || !lease.getPortalAccess().getId().equals(user().getId()))
      throw missing();
  }

  public void leaseRead(Lease lease) {
    if (lease.getPortalAccess() != null && lease.getPortalAccess().getId().equals(user().getId()))
      return;
    member(lease.getOffice().getProperty().getCompany().getId());
  }

  public static ResponseStatusException missing() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found.");
  }
}
