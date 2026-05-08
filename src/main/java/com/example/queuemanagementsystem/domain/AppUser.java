package com.example.queuemanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
@Getter
@Setter
public class AppUser extends BaseEntity {

    @Column(length = 120)
    private String firstName;

    @Column(length = 120)
    private String lastName;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false),
            indexes = {
                    @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role_id", columnList = "role_id")
            }
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Business> ownedBusinesses = new HashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Booking> bookings = new HashSet<>();

}
