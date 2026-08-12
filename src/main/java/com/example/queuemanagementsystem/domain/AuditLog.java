package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    /**
     * Admin logini (kim bajargan)
     */
    @Column(nullable = false, length = 64)
    private String adminLogin;

    /**
     * Harakat kodi: BUSINESS_STATUS_CHANGED, BUSINESS_REVIEWED, USER_DELETED ...
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AuditAction action;

    /**
     * Entity turi: BUSINESS, USER
     */
    @Column(nullable = false, length = 32)
    private String entityType;

    /**
     * Entity IDsi (UUID string)
     */
    @Column(nullable = false, length = 64)
    private String entityId;

    /**
     * Qo'shimcha ma'lumot (o'zgargan qiymatlar, izoh)
     */
    @Column(columnDefinition = "text")
    private String details;

    public AuditLog(String adminLogin, AuditAction action, String entityType, String entityId, String details) {
        this.adminLogin = adminLogin;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }
}
