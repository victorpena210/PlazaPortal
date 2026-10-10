package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "maintenance_photos")
public class MaintenancePhoto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "request_id", nullable = false)
  private MaintenanceRequest request;

  @Column(nullable = false, unique = true, length = 64)
  private String storageKey;

  @Column(nullable = false, length = 255)
  private String originalName;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public MaintenanceRequest getRequest() {
    return request;
  }

  public void setRequest(MaintenanceRequest value) {
    this.request = value;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String value) {
    this.storageKey = value;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String value) {
    this.originalName = value;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime value) {
    this.createdAt = value;
  }
}
