package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  private final MailOutboxRepository outbox;
  private final CompanyMembershipRepository memberships;
  private final String baseUrl;

  public NotificationService(
      MailOutboxRepository outbox,
      CompanyMembershipRepository memberships,
      @Value("${app.base-url}") String baseUrl) {
    this.outbox = outbox;
    this.memberships = memberships;
    this.baseUrl = baseUrl.replaceAll("/+$", "");
  }

  public String url(String path) {
    return baseUrl + path;
  }

  public void send(String to, String subject, String body) {
    if (to == null || to.isBlank()) return;
    var m = new MailOutbox();
    m.setRecipient(to);
    m.setSubject(subject);
    m.setBody(body);
    outbox.save(m);
  }

  public void owners(Company company, String subject, String body) {
    memberships.findByCompanyIdOrderById(company.getId()).stream()
        .map(m -> m.getUser().getEmail())
        .distinct()
        .forEach(email -> send(email, subject, body));
  }
}
