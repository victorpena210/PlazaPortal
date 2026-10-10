package com.victorpena.plaza.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.filter.OncePerRequestFilter;

/** Per-instance protection for authentication forms. Use one pilot instance. */
public final class AuthRateLimitFilter extends OncePerRequestFilter {
  private record Window(long start, int count) {}

  private final Map<String, Window> windows = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request.getMethod().equals("POST")
        && Set.of("/login", "/forgot-password", "/reset-password", "/register", "/join", "/signup")
            .contains(request.getServletPath())) {
      long now = System.currentTimeMillis();
      windows.entrySet().removeIf(e -> now - e.getValue().start() > 600000);
      String key = request.getRemoteAddr() + ":" + request.getServletPath();
      if (windows.size() > 10000 && !windows.containsKey(key)) {
        response.sendError(429, "Too many requests. Try again later.");
        return;
      }
      var window =
          windows.compute(
              key, (k, w) -> w == null ? new Window(now, 1) : new Window(w.start(), w.count() + 1));
      if (window.count() > 30) {
        response.setHeader("Retry-After", "600");
        response.sendError(429, "Too many attempts. Try again in ten minutes.");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
