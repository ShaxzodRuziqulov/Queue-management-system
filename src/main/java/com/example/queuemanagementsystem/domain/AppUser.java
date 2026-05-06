package com.example.queuemanagementsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.example.queuemanagementsystem.domain.enums.RoleName;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String login;

    @Column(name = "password_hash", length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(length = 320)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(length = 1024)
    private String avatarUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "user")
    private Set<UserIdentity> identities = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<RoleName> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Business> ownedBusinesses = new HashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Booking> bookings = new HashSet<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
