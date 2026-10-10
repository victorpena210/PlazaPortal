package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "companies")
public class Company {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(length = 255)
  private String supportEmail;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String value) {
    this.slug = value;
  }

  public String getSupportEmail() {
    return supportEmail;
  }

  public void setSupportEmail(String value) {
    this.supportEmail = value;
  }
}
