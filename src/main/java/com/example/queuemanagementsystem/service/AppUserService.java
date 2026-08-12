package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.domain.enums.AuditAction;
import com.example.queuemanagementsystem.dto.*;
import com.example.queuemanagementsystem.dto.auth.RegisterRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.AppUserMapper;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import com.example.queuemanagementsystem.security.AppUserDetailsService;
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
        AppUser entity = new AppUser();
        entity.setUsername(validateNewLogin(request.getLogin()));
        entity.setFirstName(request.getFirstName().trim());
        entity.setLastName(StringUtils.hasText(request.getLastName()) ? request.getLastName().trim() : null);
        entity.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        entity.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.setActive(true);
        roleRepository.assignRoleIfPresent(entity, "ROLE_USER");
        toDtoWithOwner(repository.save(entity));
    }

    public AppUserDto create(AppUserCreateRequest request) {
        String username = validateNewLogin(request.getLogin());
        AppUser entity = mapper.toEntity(request);
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        roleRepository.assignRoleIfPresent(entity, "ROLE_USER");
        return toDtoWithOwner(repository.save(entity));
    }

    /**
     * Login normallashtiradi, uzunlik va band-emasligini tekshiradi, tayyor loginni qaytaradi.
     */
    private String validateNewLogin(String login) {
        String username = AppUserDetailsService.normalizeLogin(login);
        if (username.length() < 3) {
            throw new IllegalArgumentException("Login kamida 3 belgidan iborat bo'lishi kerak");
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Bu login allaqachon band");
        }
        return username;
    }

    public AppUserDto update(UUID id, AppUserUpdateRequest request) {
        requireSelfOrAdmin(id);
        AppUser entity = requireUser(id);
        boolean wasActive = entity.isActive();
        mapper.update(entity, request);
        if (StringUtils.hasText(request.getPassword())) {
            if (!currentUserService.isAdmin()) {
                throw new AccessDeniedException("Parolni o'zgartirish uchun joriy parolni kiriting");
            }
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
                        request.getActive() ? AuditAction.USER_ACTIVATED : AuditAction.USER_DEACTIVATED,
                        "USER", id.toString(), entity.getUsername());
            } else if (request.getRoles() != null) {
                auditLogService.log(AuditAction.USER_ROLE_CHANGED, "USER", id.toString(),
                        "Yangi rollar: " + request.getRoles());
            } else {
                auditLogService.log(AuditAction.USER_UPDATED, "USER", id.toString(),
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
        auditLogService.log(AuditAction.USER_DELETED, "USER", id.toString(), entity.getUsername());
    }

    public AppUser requireUser(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi: " + id));
    }

    /**
     * Xodim uchun yangi foydalanuvchi hisobini yaratadi (biznes egasi tomonidan).
     * ROLE_STAFF alohida {@code StaffMemberService} tomonidan beriladi.
     */
    public AppUser createAccountForStaff(String login, String password, String firstName, String lastName, String email, String phone) {
        String username = validateNewLogin(login);
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Parol kamida 4 belgidan iborat bo'lishi kerak");
        }
        AppUser entity = new AppUser();
        entity.setUsername(username);
        entity.setFirstName(firstName.trim());
        entity.setLastName(StringUtils.hasText(lastName) ? lastName.trim() : null);
        entity.setEmail(StringUtils.hasText(email) ? email.trim() : null);
        entity.setPhone(StringUtils.hasText(phone) ? phone.trim() : null);
        entity.setPasswordHash(passwordEncoder.encode(password));
        entity.setActive(true);
        roleRepository.assignRoleIfPresent(entity, "ROLE_USER");
        return repository.save(entity);
    }

    /**
     * Xodimga biriktirilgan hisobning asosiy ma'lumotlarini (ism, email, telefon, parol)
     * biznes egasi tomonidan yangilash uchun — faqat bo'sh bo'lmagan maydonlar qo'llanadi.
     */
    public void updateStaffAccountFields(AppUser user, String firstName, String lastName, String email, String phone, String password) {
        if (firstName != null || lastName != null) {
            if (StringUtils.hasText(firstName)) {
                user.setFirstName(firstName.trim());
            }
            if (lastName != null) {
                user.setLastName(StringUtils.hasText(lastName) ? lastName.trim() : null);
            }
        }
        if (StringUtils.hasText(email)) user.setEmail(email.trim());
        if (StringUtils.hasText(phone)) user.setPhone(phone.trim());
        if (StringUtils.hasText(password)) {
            if (password.length() < 4) {
                throw new IllegalArgumentException("Parol kamida 4 belgidan iborat bo'lishi kerak");
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }
    }

    /**
     * Login bo'yicha foydalanuvchini qisqacha (minimal) ma'lumot bilan qaytaradi.
     * Xodimni mavjud foydalanuvchi hisobiga bog'lash uchun ishlatiladi — shu sabab
     * to'liq profil emas, faqat id/login/firstName/lastName qaytariladi.
     */
    @Transactional(readOnly = true)
    public UserLookupDto findByLogin(String login) {
        AppUser user = repository.findByUsername(AppUserDetailsService.normalizeLogin(login))
                .orElseThrow(() -> new ResourceNotFoundException("Bu login bilan foydalanuvchi topilmadi"));
        return UserLookupDto.builder()
                .id(user.getId())
                .login(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /**
     * Telefon raqami bo'yicha ro'yxatdan o'tgan foydalanuvchi(lar)ni qidiradi — xodim
     * mijozni qo'lda bron qilganda "bu mijoz tizimda bormi?" ni aniqlash uchun.
     * Topilmasa bo'sh ro'yxat qaytaradi (bu normal holat — mijoz mehmon sifatida kiritiladi).
     */
    @Transactional(readOnly = true)
    public List<UserLookupDto> findByPhone(String phone) {
        String suffix = normalizePhoneSuffix(phone);
        if (suffix.length() < 7) {
            throw new IllegalArgumentException("Telefon raqami to'liq emas");
        }
        return repository.findByPhoneEndingWith(suffix).stream()
                .map(user -> UserLookupDto.builder()
                        .id(user.getId())
                        .login(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .build())
                .toList();
    }

    /** Telefonni faqat raqamlarga keltirib, oxirgi 9 xonasini qaytaradi (998 mamlakat kodini tashlaydi). */
    private static String normalizePhoneSuffix(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        return digits.length() > 9 ? digits.substring(digits.length() - 9) : digits;
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

    public void changePassword(ChangePasswordRequest request) {
        AppUser user = requireUser(currentUserService.getCurrentUserId());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Joriy parol noto'g'ri");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    }
}
