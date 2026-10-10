package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "stored_documents")
public class StoredDocument {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lease_id", nullable = false)
  private Lease lease;

  @Column(nullable = false, length = 255)
  private String originalName;

  @Column(nullable = false, unique = true, length = 64)
  private String storageKey;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Column(nullable = false)
  private long sizeBytes;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company value) {
    this.company = value;
  }

  public Lease getLease() {
    return lease;
  }

  public void setLease(Lease value) {
    this.lease = value;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String value) {
    this.originalName = value;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String value) {
    this.storageKey = value;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String value) {
    this.contentType = value;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public void setSizeBytes(long value) {
    this.sizeBytes = value;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime value) {
    this.createdAt = value;
  }
}
