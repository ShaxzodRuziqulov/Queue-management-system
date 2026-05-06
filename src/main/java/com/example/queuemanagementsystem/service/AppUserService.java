package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.dto.AppUserCreateRequest;
import com.example.queuemanagementsystem.dto.AppUserDto;
import com.example.queuemanagementsystem.dto.AppUserUpdateRequest;
import com.example.queuemanagementsystem.dto.auth.RegisterRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.AppUserMapper;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.security.AppUserDetailsService;
import com.example.queuemanagementsystem.security.CurrentUserService;
import com.example.queuemanagementsystem.domain.enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserService {

    private final AppUserRepository repository;
    private final BusinessRepository businessRepository;
    private final AppUserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<AppUserDto> findAll() {
        return repository.findAll().stream().map(this::toDtoWithOwner).toList();
    }

    @Transactional(readOnly = true)
    public AppUserDto get(UUID id) {
        requireSelfOrAdmin(id);
        return toDtoWithOwner(requireUser(id));
    }

    public AppUserDto register(RegisterRequest request) {
        String login = AppUserDetailsService.normalizeLogin(request.getLogin());
        if (login.length() < 3) {
            throw new IllegalArgumentException("Login kamida 3 belgidan iborat bo‘lishi kerak");
        }
        if (repository.existsByLogin(login)) {
            throw new IllegalArgumentException("Bu login allaqachon band");
        }
        AppUser entity = new AppUser();
        entity.setLogin(login);
        entity.setDisplayName(request.getDisplayName().trim());
        entity.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        entity.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.setActive(true);
        entity.getRoles().add(RoleName.ROLE_USER);
        return toDtoWithOwner(repository.save(entity));
    }

    public AppUserDto create(AppUserCreateRequest request) {
        String login = AppUserDetailsService.normalizeLogin(request.getLogin());
        if (login.length() < 3) {
            throw new IllegalArgumentException("Login kamida 3 belgidan iborat bo‘lishi kerak");
        }
        if (repository.existsByLogin(login)) {
            throw new IllegalArgumentException("Bu login allaqachon band");
        }
        AppUser entity = mapper.toEntity(request);
        entity.setLogin(login);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.getRoles().add(RoleName.ROLE_USER);
        return toDtoWithOwner(repository.save(entity));
    }

    public AppUserDto update(UUID id, AppUserUpdateRequest request) {
        requireSelfOrAdmin(id);
        AppUser entity = requireUser(id);
        mapper.update(entity, request);
        if (StringUtils.hasText(request.getPassword())) {
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return toDtoWithOwner(entity);
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
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Foydalanuvchi topilmadi: " + id);
        }
        repository.deleteById(id);
    }

    AppUser requireUser(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi: " + id));
    }

    private void requireSelfOrAdmin(UUID targetId) {
        if (currentUserService.isAdmin()) return;
        UUID currentId = currentUserService.requireUserId();
        if (!currentId.equals(targetId)) {
            throw new AccessDeniedException("Faqat o'z profilingizga kira olasiz");
        }
    }

    private AppUserDto toDtoWithOwner(AppUser entity) {
        AppUserDto dto = mapper.toDto(entity);
        dto.setBusinessOwner(businessRepository.existsByOwner_Id(entity.getId()));
        return dto;
    }
}
