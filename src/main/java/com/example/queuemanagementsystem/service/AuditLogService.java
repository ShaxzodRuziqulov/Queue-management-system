package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AuditLog;
import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.enums.AuditAction;
import com.example.queuemanagementsystem.dto.AuditLogDto;
import com.example.queuemanagementsystem.repository.AuditLogRepository;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
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
        Page<AuditLog> logs = repository.search(
                normalizeExactFilter(entityType),
                parseAction(action),
                normalizeLikeFilter(adminLogin),
                pageable
        );
        Map<String, String> entityNames = loadEntityNames(logs.getContent());
        return logs.map(log -> toDto(log, entityNames.get(entityKey(log))));
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

    private Map<String, String> loadEntityNames(List<AuditLog> logs) {
        Map<String, String> names = new HashMap<>();

        List<UUID> businessIds = idsForType(logs, "BUSINESS");
        if (!businessIds.isEmpty()) {
            businessRepository.findAllById(businessIds).forEach(business ->
                    names.put(entityKey("BUSINESS", business.getId().toString()), business.getName()));
        }

        List<UUID> userIds = idsForType(logs, "USER");
        if (!userIds.isEmpty()) {
            Map<UUID, AppUser> users = appUserRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(AppUser::getId, Function.identity()));
            for (UUID id : userIds) {
                AppUser user = users.get(id);
                if (user != null) {
                    names.put(entityKey("USER", id.toString()), userDisplayName(user));
                }
            }
        }

        for (AuditLog log : logs) {
            deletedEntityName(log).ifPresent(name -> names.putIfAbsent(entityKey(log), name));
        }
        return names;
    }

    private List<UUID> idsForType(List<AuditLog> logs, String entityType) {
        return logs.stream()
                .filter(log -> entityType.equals(log.getEntityType()))
                .map(AuditLog::getEntityId)
                .map(this::parseUuid)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    private Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private String userDisplayName(AppUser user) {
        return user.getUsername();
    }

    private Optional<String> deletedEntityName(AuditLog log) {
        if (log.getAction() != AuditAction.BUSINESS_DELETED && log.getAction() != AuditAction.USER_DELETED) {
            return Optional.empty();
        }
        String details = log.getDetails();
        if (details == null || details.isBlank()) {
            return Optional.empty();
        }
        int separator = details.lastIndexOf(':');
        String name = separator >= 0 ? details.substring(separator + 1).trim() : details.trim();
        return name.isBlank() ? Optional.empty() : Optional.of(name);
    }

    private String entityKey(AuditLog log) {
        return entityKey(log.getEntityType(), log.getEntityId());
    }

    private String entityKey(String type, String id) {
        return type + ":" + id;
    }

    private AuditLogDto toDto(AuditLog log, String entityName) {
        return AuditLogDto.builder()
                .id(log.getId())
                .adminLogin(log.getAdminLogin())
                .action(log.getAction().name())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .entityName(entityName)
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
