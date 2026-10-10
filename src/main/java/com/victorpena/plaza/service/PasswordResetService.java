package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.time.*;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
  private final UserRepository users;
  private final PasswordResetTokenRepository tokens;
  private final PasswordEncoder encoder;
  private final NotificationService mail;
  private final Clock clock;
  private final SessionRegistry sessions;

  public PasswordResetService(
      UserRepository users,
      PasswordResetTokenRepository tokens,
      PasswordEncoder encoder,
      NotificationService mail,
      Clock clock,
      SessionRegistry sessions) {
    this.users = users;
    this.tokens = tokens;
    this.encoder = encoder;
    this.mail = mail;
    this.clock = clock;
    this.sessions = sessions;
  }

  @Transactional
  public void request(String email) {
    if (email == null) return;
    users
        .findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
        .filter(User::isEnabled)
        .ifPresent(
            u -> {
              var current = tokens.findByUserIdAndUsedFalse(u.getId());
              if (current.stream()
                  .anyMatch(
                      t -> t.getExpiresAt().isAfter(LocalDateTime.now(clock).plusMinutes(55))))
                return;
              current.forEach(t -> t.setUsed(true));
              String raw = Tokens.create();
              var t = new PasswordResetToken();
              t.setUser(u);
              t.setTokenHash(Tokens.hash(raw));
              t.setExpiresAt(LocalDateTime.now(clock).plusHours(1));
              tokens.save(t);
              mail.send(
                  u.getEmail(),
                  "Reset your PlazaPortal password",
                  "Use this one-time link within one hour: "
                      + mail.url("/reset-password?token=" + raw));
            });
  }

  @Transactional
  public void reset(String raw, String password) {
    Checks.password(password);
    var t =
        tokens
            .lockByHash(Tokens.hash(raw))
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset link."));
    if (t.isUsed() || !t.getExpiresAt().isAfter(LocalDateTime.now(clock)))
      throw new IllegalArgumentException("Reset link expired or already used.");
    var u = t.getUser();
    u.setPassword(encoder.encode(password));
    t.setUsed(true);
    for (var principal : sessions.getAllPrincipals()) {
      if (principal instanceof org.springframework.security.core.userdetails.UserDetails details
          && details.getUsername().equals(u.getEmail()))
        sessions.getAllSessions(principal, false).forEach(s -> s.expireNow());
    }
  }
}
