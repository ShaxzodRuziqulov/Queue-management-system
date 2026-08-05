package com.example.queuemanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staff_members")
@Getter
@Setter
public class StaffMember extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 120)
    private String firstName;

    @Column(length = 120)
    private String lastName;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id")
    private AppUser linkedUser;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "staff")
    private Set<Booking> bookings = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "staff_member_services",
            joinColumns = @JoinColumn(name = "staff_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<OfferedService> offeredServices = new HashSet<>();

    public String getFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String fullName = (first + " " + last).trim();
        return fullName;
    }
}
