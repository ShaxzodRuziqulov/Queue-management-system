package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.dto.AppUserCreateRequest;
import com.example.queuemanagementsystem.dto.AppUserDto;
import com.example.queuemanagementsystem.dto.AppUserUpdateRequest;
import com.example.queuemanagementsystem.dto.UserLookupDto;
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
        AppUser entity = new AppUser();
        entity.setUsername(validateNewLogin(request.getLogin()));
        entity.setDisplayName(request.getDisplayName().trim());
        applyNameSplit(entity, null, null);
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
        applyNameSplit(entity, request.getFirstName(), request.getLastName());
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        roleRepository.assignRoleIfPresent(entity, "ROLE_USER");
        return toDtoWithOwner(repository.save(entity));
    }

    /** Login normallashtiradi, uzunlik va band-emasligini tekshiradi, tayyor loginni qaytaradi. */
    private String validateNewLogin(String login) {
        String username = normalizeLogin(login);
        if (username.length() < 3) {
            throw new IllegalArgumentException("Login kamida 3 belgidan iborat bo'lishi kerak");
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Bu login allaqachon band");
        }
        return username;
    }

    /**
     * firstName/lastName aniq berilmagan bo'lsa, displayName'dan avtomatik ajratib to'ldiradi
     * — har bir foydalanuvchi yozuvida bu ikki ustun bo'sh qolmasligi uchun.
     */
    private void applyNameSplit(AppUser entity, String explicitFirstName, String explicitLastName) {
        if (StringUtils.hasText(explicitFirstName) || StringUtils.hasText(explicitLastName)) {
            return;
        }
        String source = StringUtils.hasText(entity.getDisplayName()) ? entity.getDisplayName() : entity.getUsername();
        String[] parts = source.trim().split("\\s+", 2);
        entity.setFirstName(parts[0]);
        entity.setLastName(parts.length > 1 ? parts[1] : null);
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

    /**
     * Xodim uchun yangi foydalanuvchi hisobini yaratadi (biznes egasi tomonidan).
     * ROLE_STAFF alohida {@code StaffMemberService} tomonidan beriladi.
     */
    public AppUser createAccountForStaff(String login, String password, String displayName, String email, String phone) {
        String username = validateNewLogin(login);
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Parol kamida 4 belgidan iborat bo'lishi kerak");
        }
        AppUser entity = new AppUser();
        entity.setUsername(username);
        entity.setDisplayName(StringUtils.hasText(displayName) ? displayName.trim() : username);
        applyNameSplit(entity, null, null);
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
    public void updateStaffAccountFields(AppUser user, String displayName, String email, String phone, String password) {
        if (StringUtils.hasText(displayName)) {
            user.setDisplayName(displayName.trim());
            applyNameSplit(user, null, null);
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
     * to'liq profil emas, faqat id/login/displayName qaytariladi.
     */
    @Transactional(readOnly = true)
    public UserLookupDto findByLogin(String login) {
        AppUser user = repository.findByUsername(normalizeLogin(login))
                .orElseThrow(() -> new ResourceNotFoundException("Bu login bilan foydalanuvchi topilmadi"));
        return UserLookupDto.builder()
                .id(user.getId())
                .login(user.getUsername())
                .displayName(user.getDisplayName())
                .build();
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
