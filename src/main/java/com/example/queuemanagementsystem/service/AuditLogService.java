package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AuditLog;
import com.example.queuemanagementsystem.dto.AuditLogDto;
import com.example.queuemanagementsystem.repository.AuditLogRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository repository;
    private final CurrentUserService currentUserService;

    // ── Harakat kodlari ─────────────────────────────────────────────────────
    public static final String BUSINESS_STATUS_CHANGED = "BUSINESS_STATUS_CHANGED";
    public static final String BUSINESS_REVIEWED       = "BUSINESS_REVIEWED";
    public static final String BUSINESS_DELETED        = "BUSINESS_DELETED";
    public static final String USER_CREATED            = "USER_CREATED";
    public static final String USER_UPDATED            = "USER_UPDATED";
    public static final String USER_ACTIVATED          = "USER_ACTIVATED";
    public static final String USER_DEACTIVATED        = "USER_DEACTIVATED";
    public static final String USER_ROLE_CHANGED       = "USER_ROLE_CHANGED";
    public static final String USER_DELETED            = "USER_DELETED";

    /**
     * Hozirgi admin nomidan log yozadi.
     * Alohida tranzaksiyada ishlaydi — asosiy tranzaksiya rollback bo'lsa ham log saqlanadi.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, String entityId, String details) {
        String adminLogin = currentUserService.getCurrentUsername();
        if (adminLogin == null) adminLogin = "system";
        try {
            repository.save(new AuditLog(adminLogin, action, entityType, entityId, details));
        } catch (Exception e) {
            log.error("Audit log yozishda xatolik: action={} entityId={}", action, entityId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> findAll(String entityType, String action, String adminLogin, Pageable pageable) {
        return repository.findFiltered(
                entityType,
                action,
                adminLogin,
                pageable
        ).map(this::toDto);
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .adminLogin(log.getAdminLogin())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .build();
    }
}
