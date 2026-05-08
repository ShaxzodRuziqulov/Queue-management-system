package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.dto.AppUserCreateRequest;
import com.example.queuemanagementsystem.dto.AppUserDto;
import com.example.queuemanagementsystem.dto.AppUserUpdateRequest;
import com.example.queuemanagementsystem.dto.auth.RegisterRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.AppUserMapper;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserService {

    private final AppUserRepository repository;
    private final BusinessRepository businessRepository;
    private final RoleRepository roleRepository;
    private final AppUserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<AppUserDto> findAll() {
        return repository.findAllWithRoles().stream().map(this::toDtoWithOwner).toList();
    }

    @Transactional(readOnly = true)
    public AppUserDto get(UUID id) {
        requireSelfOrAdmin(id);
        return toDtoWithOwner(requireUser(id));
    }

    public void register(RegisterRequest request) {
        String username = normalizeLogin(request.getLogin());
        if (username.length() < 3) {
            throw new IllegalArgumentException("Login kamida 3 belgidan iborat bo'lishi kerak");
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Bu login allaqachon band");
        }
        AppUser entity = new AppUser();
        entity.setUsername(username);
        entity.setDisplayName(request.getDisplayName().trim());
        entity.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        entity.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.setActive(true);
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole != null) entity.getRoles().add(userRole);
        toDtoWithOwner(repository.save(entity));
    }

    public AppUserDto create(AppUserCreateRequest request) {
        String username = normalizeLogin(request.getLogin());
        if (username.length() < 3) {
            throw new IllegalArgumentException("Login kamida 3 belgidan iborat bo'lishi kerak");
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Bu login allaqachon band");
        }
        AppUser entity = mapper.toEntity(request);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole != null) entity.getRoles().add(userRole);
        return toDtoWithOwner(repository.save(entity));
    }

    public AppUserDto update(UUID id, AppUserUpdateRequest request) {
        requireSelfOrAdmin(id);
        AppUser entity = requireUser(id);
        boolean wasActive = entity.isActive();
        mapper.update(entity, request);
        if (StringUtils.hasText(request.getPassword())) {
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoles() != null && currentUserService.isAdmin()) {
            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleRepository::findByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            entity.setRoles(newRoles);
        }
        AppUserDto result = toDtoWithOwner(entity);

        if (currentUserService.isAdmin()) {
            if (request.getActive() != null && request.getActive() != wasActive) {
                auditLogService.log(
                        request.getActive() ? AuditLogService.USER_ACTIVATED : AuditLogService.USER_DEACTIVATED,
                        "USER", id.toString(), entity.getUsername());
            } else if (request.getRoles() != null) {
                auditLogService.log(AuditLogService.USER_ROLE_CHANGED, "USER", id.toString(),
                        "Yangi rollar: " + request.getRoles());
            } else {
                auditLogService.log(AuditLogService.USER_UPDATED, "USER", id.toString(),
                        entity.getUsername());
            }
        }
        return result;
    }

    public AppUserDto uploadAvatar(UUID id, MultipartFile file) {
        requireSelfOrAdmin(id);
        AppUser entity = requireUser(id);
        if (entity.getAvatarUrl() != null) {
            fileStorageService.delete(entity.getAvatarUrl());
        }
        String url = fileStorageService.store(file, "avatars");
        entity.setAvatarUrl(url);
        return toDtoWithOwner(entity);
    }

    public void delete(UUID id) {
        AppUser entity = requireUser(id);
        repository.deleteById(id);
        auditLogService.log(AuditLogService.USER_DELETED, "USER", id.toString(), entity.getUsername());
    }

    public AppUser requireUser(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi: " + id));
    }

    private void requireSelfOrAdmin(UUID targetId) {
        if (currentUserService.isAdmin()) return;
        UUID currentId = currentUserService.getCurrentUserId();
        if (!currentId.equals(targetId)) {
            throw new AccessDeniedException("Faqat o'z profilingizga kira olasiz");
        }
    }

    private AppUserDto toDtoWithOwner(AppUser entity) {
        AppUserDto dto = mapper.toDto(entity);
        dto.setBusinessOwner(businessRepository.existsByOwner_Id(entity.getId()));
        return dto;
    }

    private static String normalizeLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase();
    }
}
