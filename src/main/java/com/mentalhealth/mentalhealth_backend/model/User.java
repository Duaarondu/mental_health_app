package com.mentalhealth.mentalhealth_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Maps the USER table in database/schema.sql. */
@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt hash. The plain password is never stored. */
    @Column(name = "passwordHash", nullable = false)
    private String passwordHash;

    /** One of the values in {@link Role}. */
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "firstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "isVerified")
    private Boolean isVerified = false;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isTherapist() {
        return Role.THERAPIST.equals(role);
    }

    public boolean isAnonymous() {
        return Role.ANONYMOUS.equals(role);
    }

    /** Clients and anonymous guests are both "patients". */
    public boolean isPatient() {
        return Role.CLIENT.equals(role) || Role.ANONYMOUS.equals(role);
    }

    /** Name shown to other people. Anonymous users only ever show their nickname. */
    public String getDisplayName() {
        if (isAnonymous()) {
            return firstName + " (anonymous)";
        }
        if (isTherapist()) {
            return "Dr. " + lastName;
        }
        return firstName + " " + lastName;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
