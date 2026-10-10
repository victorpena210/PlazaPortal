package com.victorpena.plaza.config;

import com.victorpena.plaza.controller.CustomLoginSuccessHandler;
import com.victorpena.plaza.service.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, CustomLoginSuccessHandler handler, SessionRegistry registry)
      throws Exception {
    http.addFilterBefore(
            new AuthRateLimitFilter(),
            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
                .class)
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/join",
                        "/signup",
                        "/forgot-password",
                        "/reset-password",
                        "/css/**",
                        "/images/**",
                        "/error",
                        "/error/**",
                        "/webhooks/stripe",
                        "/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .csrf(c -> c.ignoringRequestMatchers("/webhooks/stripe"))
        .formLogin(
            f ->
                f.loginPage("/login")
                    .successHandler(handler)
                    .failureUrl("/login?error")
                    .permitAll())
        .logout(
            l ->
                l.logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"))
        .sessionManagement(s -> s.maximumSessions(-1).sessionRegistry(registry))
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src"
                                + " 'self'; frame-ancestors 'none'; base-uri 'self'; form-action"
                                + " 'self'")))
        .exceptionHandling(e -> e.accessDeniedPage("/error/403"));
    return http.build();
  }

  @Bean
  DaoAuthenticationProvider authenticationProvider(
      CustomUserDetailsService details, PasswordEncoder encoder) {
    var p = new DaoAuthenticationProvider(details);
    p.setPasswordEncoder(encoder);
    return p;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
