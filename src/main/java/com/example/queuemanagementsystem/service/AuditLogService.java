package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AuditLog;
import com.example.queuemanagementsystem.domain.enums.AuditAction;
import com.example.queuemanagementsystem.dto.AuditLogDto;
import com.example.queuemanagementsystem.repository.AuditLogRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;
    private final CurrentUserService currentUserService;

    /**
     * Hozirgi admin nomidan log yozadi.
     * Alohida tranzaksiyada ishlaydi - asosiy tranzaksiya rollback bo'lsa ham log saqlanadi.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, String entityId, String details) {
        String adminLogin = currentUserService.getCurrentUsername();
        if (adminLogin == null) adminLogin = "system";
        repository.saveAndFlush(new AuditLog(adminLogin, action, entityType, entityId, details));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> findAll(String entityType, String action, String adminLogin, Pageable pageable) {
        return repository.search(
                normalizeExactFilter(entityType),
                parseAction(action),
                normalizeLikeFilter(adminLogin),
                pageable
        ).map(this::toDto);
    }

    private AuditAction parseAction(String action) {
        if (action == null || action.isBlank()) {
            return null;
        }
        return AuditAction.valueOf(action.trim().toUpperCase(Locale.ROOT));
    }

    private String normalizeExactFilter(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String normalizeLikeFilter(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .adminLogin(log.getAdminLogin())
                .action(log.getAction().name())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
