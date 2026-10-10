package com.victorpena.plaza.service;

import com.victorpena.plaza.repository.*;
import java.time.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MailDeliveryService {
  private final MailOutboxRepository outbox;
  private final ObjectProvider<JavaMailSender> sender;
  private final boolean enabled;
  private final String from;

  public MailDeliveryService(
      MailOutboxRepository outbox,
      ObjectProvider<JavaMailSender> sender,
      @Value("${app.mail-enabled:false}") boolean enabled,
      @Value("${app.mail-from:}") String from) {
    this.outbox = outbox;
    this.sender = sender;
    this.enabled = enabled;
    this.from = from;
  }

  @Scheduled(fixedDelayString = "${app.mail-delay-ms:60000}")
  public synchronized void deliver() {
    if (!enabled || sender.getIfAvailable() == null) return;
    for (var m :
        outbox.findTop20BySentAtIsNullAndNextAttemptAtBeforeOrderById(LocalDateTime.now())) {
      try {
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(m.getRecipient());
        message.setSubject(m.getSubject());
        message.setText(m.getBody());
        sender.getObject().send(message);
        m.setSentAt(LocalDateTime.now());
        m.setBody("Delivered notification. Content removed.");
      } catch (Exception ex) {
        m.setAttempts(m.getAttempts() + 1);
        m.setNextAttemptAt(LocalDateTime.now().plusMinutes(Math.min(360, 5L * m.getAttempts())));
      }
      outbox.save(m);
    }
  }
}
