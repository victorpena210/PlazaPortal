package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_webhooks")
public class ProcessedWebhook {
  @Id
  @Column(length = 255)
  private String id;

  private LocalDateTime processedAt;

  public ProcessedWebhook() {}

  public ProcessedWebhook(String id) {
    this.id = id;
    this.processedAt = LocalDateTime.now();
  }
}
