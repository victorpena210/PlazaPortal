package com.victorpena.plaza.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
    name = "company_memberships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "user_id"}))
public class CompanyMembership {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MembershipRole role;

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

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    this.user = value;
  }

  public MembershipRole getRole() {
    return role;
  }

  public void setRole(MembershipRole value) {
    this.role = value;
  }
}
