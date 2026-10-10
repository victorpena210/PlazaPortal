package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public UserService(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Transactional
  public User registerUser(String first, String last, String email, String password, Role role) {
    email = Checks.email(email);
    Checks.password(password);
    if (users.existsByEmail(email))
      throw new IllegalArgumentException(
          "That email already has an account. Sign in or reset its password.");
    var u = new User();
    u.setFirstName(Checks.text(first, "First name", 100));
    u.setLastName(Checks.text(last, "Last name", 100));
    u.setEmail(email);
    u.setPassword(encoder.encode(password));
    u.setRole(role);
    u.setEnabled(true);
    return users.saveAndFlush(u);
  }

  public Optional<User> findByEmail(String email) {
    return users.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
  }

  public boolean emailExists(String email) {
    return users.existsByEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
  }
}
