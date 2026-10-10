package com.victorpena.plaza.service;

import java.math.*;
import java.time.*;
import java.util.Locale;

public final class Checks {
  private Checks() {}

  public static String text(String value, String label, int max) {
    if (value == null || value.isBlank() || value.trim().length() > max)
      throw new IllegalArgumentException(label + " is required (maximum " + max + " characters).");
    return value.trim();
  }

  public static String email(String value) {
    String s = text(value, "Email", 255).toLowerCase(Locale.ROOT);
    if (!s.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
      throw new IllegalArgumentException("Enter a valid email.");
    return s;
  }

  public static BigDecimal money(BigDecimal value, String label, boolean zero) {
    if (value == null
        || value.scale() > 2
        || value.compareTo(new BigDecimal("9999999.99")) > 0
        || value.signum() < 0
        || (!zero && value.signum() == 0))
      throw new IllegalArgumentException(
          label
              + " must be "
              + (zero ? "zero or " : "")
              + "a positive amount with at most two decimals.");
    return value.setScale(2);
  }

  public static void password(String password) {
    if (password == null
        || password.length() < 12
        || password.length() > 72
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException(
          "Use a password of 12–72 characters (maximum 72 UTF-8 bytes).");
  }
}
