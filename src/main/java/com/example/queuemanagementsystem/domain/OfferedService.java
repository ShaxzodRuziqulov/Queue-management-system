package com.example.queuemanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offered_services")
@Getter
@Setter
public class OfferedService extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Xizmat uchun reklama rasmi (ixtiyoriy)
     */
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @OneToMany(mappedBy = "offeredService")
    private Set<Booking> bookings = new HashSet<>();

    @ManyToMany(mappedBy = "offeredServices")
    private Set<StaffMember> staffMembers = new HashSet<>();
}
