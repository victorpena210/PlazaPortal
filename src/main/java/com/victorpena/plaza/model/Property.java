package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "properties")
public class Property {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, length = 500)
  private String address;

  @Column(nullable = false)
  private boolean active = true;

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

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    this.address = value;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }
}
