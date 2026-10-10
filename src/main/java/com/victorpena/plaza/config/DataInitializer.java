package com.victorpena.plaza.config;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class DataInitializer {
  @Bean
  CommandLineRunner bootstrap(
      UserRepository users,
      CompanyRepository companies,
      CompanyMembershipRepository members,
      UserService service,
      @Value("${app.bootstrap-email:}") String email,
      @Value("${app.bootstrap-password:}") String password) {
    return args -> {
      if (email.isBlank() || password.isBlank()) return;
      if (users.existsByEmail(Checks.email(email))) return;
      var u =
          service.registerUser(
              "Company", "Owner", email, password, com.victorpena.plaza.model.Role.ADMIN);
      var c =
          companies
              .findBySlug("pena-plaza")
              .orElseGet(
                  () -> {
                    var n = new Company();
                    n.setName("Peña Plaza");
                    n.setSlug("pena-plaza");
                    n.setSupportEmail(email);
                    return companies.save(n);
                  });
      var m = new CompanyMembership();
      m.setCompany(c);
      m.setUser(u);
      m.setRole(MembershipRole.OWNER);
      members.save(m);
    };
  }
}
