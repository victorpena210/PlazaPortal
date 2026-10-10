package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
  private final CompanyRepository companies;
  private final CompanyMembershipRepository members;
  private final MembershipInvitationRepository invites;
  private final UserRepository users;
  private final UserService userService;
  private final AccessService access;
  private final NotificationService mail;
  private final AuditService audit;
  private final Clock clock;

  public CompanyService(
      CompanyRepository companies,
      CompanyMembershipRepository members,
      MembershipInvitationRepository invites,
      UserRepository users,
      UserService userService,
      AccessService access,
      NotificationService mail,
      AuditService audit,
      Clock clock) {
    this.companies = companies;
    this.members = members;
    this.invites = invites;
    this.users = users;
    this.userService = userService;
    this.access = access;
    this.mail = mail;
    this.audit = audit;
    this.clock = clock;
  }

  @Transactional
  public Company create(User user, String name) {
    var c = new Company();
    c.setName(Checks.text(name, "Company name", 150));
    c.setSlug(UUID.randomUUID().toString());
    c.setSupportEmail(user.getEmail());
    companies.save(c);
    var m = new CompanyMembership();
    m.setCompany(c);
    m.setUser(user);
    m.setRole(MembershipRole.OWNER);
    members.save(m);
    audit.record(c, user.getEmail(), "COMPANY_CREATED", "Company created");
    return c;
  }

  @Transactional
  public void signup(String first, String last, String email, String password, String name) {
    var u = userService.registerUser(first, last, email, password, Role.ADMIN);
    create(u, name);
  }

  @Transactional
  public void update(Company c, String name, String support) {
    access.owner(c.getId());
    c.setName(Checks.text(name, "Company name", 150));
    c.setSupportEmail(Checks.email(support));
    companies.save(c);
    audit.record(c, access.user().getEmail(), "COMPANY_UPDATED", "Company settings updated");
  }

  @Transactional
  public void invite(Company c, String email, MembershipRole role) {
    access.owner(c.getId());
    email = Checks.email(email);
    for (var old : invites.findByCompanyIdAndEmailAndUsedFalse(c.getId(), email)) old.setUsed(true);
    String raw = Tokens.create();
    var i = new MembershipInvitation();
    i.setCompany(c);
    i.setEmail(email);
    i.setRole(role);
    i.setTokenHash(Tokens.hash(raw));
    i.setExpiresAt(LocalDateTime.now(clock).plusDays(7));
    invites.save(i);
    mail.send(
        email,
        "Join " + c.getName(),
        "You have been invited as "
            + role
            + ". Accept within seven days: "
            + mail.url("/join?token=" + raw));
    audit.record(c, access.user().getEmail(), "MEMBER_INVITED", "Role " + role + " invited");
  }

  @Transactional
  public MembershipInvitation valid(String raw) {
    var i =
        invites
            .lockByHash(Tokens.hash(raw))
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
    if (i.isUsed() || !i.getExpiresAt().isAfter(LocalDateTime.now(clock)))
      throw new IllegalArgumentException("Invitation expired or already used.");
    return i;
  }

  @Transactional
  public void accept(String raw, String first, String last, String password, User signedIn) {
    var i = valid(raw);
    var existing = users.findByEmail(i.getEmail());
    User u;
    if (existing.isPresent()) {
      if (signedIn == null || !signedIn.getId().equals(existing.get().getId()))
        throw new IllegalArgumentException("Sign in with the invited email first.");
      u = existing.get();
    } else u = userService.registerUser(first, last, i.getEmail(), password, Role.ADMIN);
    var member = members.findByCompanyIdAndUserId(i.getCompany().getId(), u.getId());
    if (member.isEmpty()) {
      var m = new CompanyMembership();
      m.setCompany(i.getCompany());
      m.setUser(u);
      m.setRole(i.getRole());
      members.save(m);
    }
    i.setUsed(true);
    audit.record(i.getCompany(), u.getEmail(), "MEMBER_JOINED", "Invitation accepted");
  }

  @Transactional
  public void remove(Company c, Long id) {
    companies.lockById(c.getId()).orElseThrow(AccessService::missing);
    access.owner(c.getId());
    var member = members.findById(id).orElseThrow(AccessService::missing);
    access.sameCompany(c.getId(), member.getCompany());
    if (member.getRole() == MembershipRole.OWNER
        && members.countByCompanyIdAndRole(c.getId(), MembershipRole.OWNER) <= 1)
      throw new IllegalArgumentException("The company must retain at least one owner.");
    members.delete(member);
    audit.record(c, access.user().getEmail(), "MEMBER_REMOVED", "Membership " + id);
  }
}
