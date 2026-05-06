package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String addressLine;

    @Column(length = 120)
    private String city;

    /**
     * WGS-84 kenglik (yaqin joylar qidiruvi uchun).
     */
    @Column(precision = 11, scale = 8)
    private BigDecimal latitude;

    /**
     * WGS-84 uzunlik.
     */
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 32)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BusinessStatus status = BusinessStatus.DRAFT;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Bepul sinov davri tugash sanasi (biznes ochilganda +14 kun).
     * NULL bo'lsa sinov davri mavjud emas (to'g'ridan-to'g'ri ACTIVE).
     */
    @Column(name = "trial_end_date")
    private Instant trialEndDate;

    /**
     * Pullik obuna tugash sanasi.
     * NULL yoki o'tib ketgan bo'lsa – obuna faol emas.
     */
    @Column(name = "subscription_end_date")
    private Instant subscriptionEndDate;

    /** Obuna yoki sinov davri hozir faolmi? */
    public boolean isAccessAllowed() {
        Instant now = Instant.now();
        if (status == BusinessStatus.ACTIVE) return true;
        if (status == BusinessStatus.TRIAL && trialEndDate != null && now.isBefore(trialEndDate)) return true;
        if (subscriptionEndDate != null && now.isBefore(subscriptionEndDate)) return true;
        return false;
    }

    @OneToMany(mappedBy = "business")
    private Set<BusinessHours> hours = new HashSet<>();

    @OneToMany(mappedBy = "business")
    private Set<OfferedService> offeredServices = new HashSet<>();

    @OneToMany(mappedBy = "business")
    private Set<StaffMember> staffMembers = new HashSet<>();

    @OneToMany(mappedBy = "business")
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
