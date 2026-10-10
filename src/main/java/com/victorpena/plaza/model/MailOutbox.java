package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "mail_outbox")
public class MailOutbox {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String recipient;

  @Column(nullable = false, length = 255)
  private String subject;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(nullable = false)
  private int attempts = 0;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime sentAt;

  @Column(nullable = false)
  private LocalDateTime nextAttemptAt = LocalDateTime.now();

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String value) {
    this.recipient = value;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String value) {
    this.subject = value;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    this.body = value;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int value) {
    this.attempts = value;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime value) {
    this.createdAt = value;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalDateTime value) {
    this.sentAt = value;
  }

  public LocalDateTime getNextAttemptAt() {
    return nextAttemptAt;
  }

  public void setNextAttemptAt(LocalDateTime value) {
    this.nextAttemptAt = value;
  }
}
