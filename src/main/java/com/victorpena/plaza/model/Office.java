package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
    name = "offices",
    uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "office_number"}))
public class Office {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  @Column(nullable = false, length = 20)
  private String officeNumber;

  private Integer squareFeet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OfficeStatus status = OfficeStatus.AVAILABLE;

  @Column(length = 255)
  private String description;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Property getProperty() {
    return property;
  }

  public void setProperty(Property value) {
    this.property = value;
  }

  public String getOfficeNumber() {
    return officeNumber;
  }

  public void setOfficeNumber(String value) {
    this.officeNumber = value;
  }

  public Integer getSquareFeet() {
    return squareFeet;
  }

  public void setSquareFeet(Integer value) {
    this.squareFeet = value;
  }

  public OfficeStatus getStatus() {
    return status;
  }

  public void setStatus(OfficeStatus value) {
    this.status = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }
}
