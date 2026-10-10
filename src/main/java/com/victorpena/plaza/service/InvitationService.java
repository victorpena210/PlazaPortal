package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {
  private final TenantInvitationRepository invites;
  private final LeaseRepository leases;
  private final UserRepository users;
  private final UserService userService;
  private final NotificationService mail;
  private final Clock clock;

  public InvitationService(
      TenantInvitationRepository invites,
      LeaseRepository leases,
      UserRepository users,
      UserService userService,
      NotificationService mail,
      Clock clock) {
    this.invites = invites;
    this.leases = leases;
    this.users = users;
    this.userService = userService;
    this.mail = mail;
    this.clock = clock;
  }

  @Transactional
  public void sendInvitation(Lease lease) {
    for (var old : invites.findByLeaseIdAndUsedFalse(lease.getId())) {
      old.setUsed(true);
    }
    String raw = Tokens.create();
    var i = new TenantInvitation();
    i.setLease(lease);
    i.setToken(Tokens.hash(raw));
    i.setExpiresAt(LocalDateTime.now(clock).plusDays(7));
    invites.save(i);
    mail.send(
        lease.getTenantEmail(),
        "Your tenant account invitation",
        "You have been invited to "
            + lease.getOffice().getProperty().getCompany().getName()
            + ". Accept within seven days: "
            + mail.url("/register?token=" + raw));
  }

  @Transactional
  public TenantInvitation valid(String raw) {
    var i =
        invites
            .lockByToken(Tokens.hash(raw))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "This invitation is invalid. Request a new invitation."));
    if (i.isUsed() || !i.getExpiresAt().isAfter(LocalDateTime.now(clock)))
      throw new IllegalArgumentException("This invitation has expired or was already used.");
    return i;
  }

  @Transactional
  public void accept(String raw, String first, String last, String password, User signedIn) {
    var i = valid(raw);
    var l = leases.lockById(i.getLease().getId()).orElseThrow();
    String email = Checks.email(l.getTenantEmail());
    var existing = users.findByEmail(email);
    User user;
    if (existing.isPresent()) {
      if (signedIn == null || !existing.get().getId().equals(signedIn.getId()))
        throw new IllegalArgumentException("Sign in with the invited email to accept this lease.");
      user = existing.get();
    } else {
      user = userService.registerUser(first, last, email, password, Role.TENANT);
    }
    l.setPortalAccess(user);
    i.setUsed(true);
  }
}
