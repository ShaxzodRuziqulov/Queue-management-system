package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.BusinessCategory;
import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@ToString(onlyExplicitlyIncluded = true)
public class Business extends BaseEntity{

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
    private BusinessCategory category = BusinessCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BusinessStatus status = BusinessStatus.DRAFT;

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

    // ── Review workflow ──────────────────────────────────────────────────────

    /** PENDING_REVIEW → ACTIVE/DRAFT o'tish vaqtida admin yozgan izoh */
    @Column(name = "review_note", columnDefinition = "text")
    private String reviewNote;

    /** Kim ko'rib chiqdi (admin logini) */
    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;

    /** Qachon ko'rib chiqildi */
    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /** Obuna yoki sinov davri hozir faolmi? (yozuv amallariga ruxsat shu bilan boshqariladi) */
    public boolean isAccessAllowed() {
        Instant now = Instant.now();
        return switch (status) {
            // ACTIVE: obuna muddati bo'lsa tekshiriladi; NULL bo'lsa cheksiz faol.
            case ACTIVE -> subscriptionEndDate == null || now.isBefore(subscriptionEndDate);
            // TRIAL: sinov muddati o'tmagan bo'lsa faol.
            case TRIAL -> trialEndDate != null && now.isBefore(trialEndDate);
            // DRAFT, PENDING_REVIEW, EXPIRED, SUSPENDED — yozuvga ruxsat yo'q.
            default -> false;
        };
    }

    @OneToMany(mappedBy = "business")
    private Set<BusinessHours> hours = new HashSet<>();

    @OneToMany(mappedBy = "business")
    private Set<OfferedService> offeredServices = new HashSet<>();

    @OneToMany(mappedBy = "business")
    private Set<StaffMember> staffMembers = new HashSet<>();

    @OneToMany(mappedBy = "business")
    private Set<Booking> bookings = new HashSet<>();
}
