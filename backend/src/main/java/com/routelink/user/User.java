package com.routelink.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(
    name = "users",
    uniqueConstraints = { @UniqueConstraint(name = "uq_users_email", columnNames = "email") }
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @Email
  @Column(nullable = false, unique = true)
  private String email;

  @Column(length = 32)
  private String phone;

  // --- Authentication fields ---
  @Column(nullable = false, length = 120)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Role role = Role.RIDER; // Default role

  // --- Profile / metadata ---
  @Column(nullable = false)
  private boolean verified = false;

  @Column(nullable = false)
  private double ratingAvg = 0.0;

  @Column(nullable = false)
  private int ratingCount = 0;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    this.createdAt = Instant.now();
    if (email != null) email = email.trim().toLowerCase();
    if (name != null)  name  = name.trim();
    if (phone != null) phone = phone.trim();
  }

  @PreUpdate
  void onUpdate() {
    if (email != null) email = email.trim().toLowerCase();
    if (name != null)  name  = name.trim();
    if (phone != null) phone = phone.trim();
  }

  // --- Getters and Setters ---
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public Role getRole() { return role; }
  public void setRole(Role role) { this.role = role; }

  public boolean isVerified() { return verified; }
  public void setVerified(boolean verified) { this.verified = verified; }

  public double getRatingAvg() { return ratingAvg; }
  public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }

  public int getRatingCount() { return ratingCount; }
  public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
