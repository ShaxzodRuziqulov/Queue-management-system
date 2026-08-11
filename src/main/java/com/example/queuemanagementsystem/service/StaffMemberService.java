package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import com.example.queuemanagementsystem.dto.BookingDto;
import com.example.queuemanagementsystem.dto.StaffAccountUpdateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberCreateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffMemberUpdateRequest;
import com.example.queuemanagementsystem.dto.StaffRegisterRequest;
import com.example.queuemanagementsystem.dto.StaffStatsDto;
import com.example.queuemanagementsystem.dto.UserLookupDto;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.BookingMapper;
import com.example.queuemanagementsystem.mapper.StaffMemberMapper;
import com.example.queuemanagementsystem.repository.BookingRepository;
import com.example.queuemanagementsystem.repository.OfferedServiceRepository;
import com.example.queuemanagementsystem.repository.ReviewRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffMemberService {

    private final StaffMemberRepository repository;
    private final StaffMemberMapper mapper;
    private final BookingMapper bookingMapper;
    private final BusinessService businessService;
    private final AppUserService userService;
    private final CurrentUserService currentUserService;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final OfferedServiceRepository offeredServiceRepository;

    @Transactional(readOnly = true)
    public List<StaffMemberDto> findAll(UUID businessId) {
        businessService.requireBusiness(businessId);
        return repository.findByBusiness_Id(businessId).stream().map(this::toDtoWithStats).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffMemberDto> findByService(UUID businessId, UUID serviceId) {
        businessService.requireBusiness(businessId);
        return repository.findActiveByBusinessIdAndServiceId(businessId, serviceId)
                .stream()
                .map(this::toDtoWithStats)
                .toList();
    }

    @Transactional(readOnly = true)
    public StaffMemberDto get(UUID businessId, UUID staffId) {
        return toDtoWithStats(requireStaff(businessId, staffId));
    }

    /** Joriy foydalanuvchining xodim profilini qaytaradi */
    @Transactional(readOnly = true)
    public StaffMemberDto getMyProfile() {
        UUID userId = currentUserService.getCurrentUserId();
        return toDtoWithStats(requireStaffByUserId(userId));
    }

    /** Joriy xodimga biriktirilgan bronlarni qaytaradi */
    @Transactional(readOnly = true)
    public List<BookingDto> getMyBookings() {
        UUID userId = currentUserService.getCurrentUserId();
        StaffMember staff = requireStaffByUserId(userId);
        return bookingRepository.findByStaff_Id(staff.getId())
                .stream().map(bookingMapper::toDto).toList();
    }

    /** Joriy xodimning statistikasini qaytaradi */
    @Transactional(readOnly = true)
    public StaffStatsDto getMyStats() {
        UUID userId = currentUserService.getCurrentUserId();
        StaffMember staff = requireStaffByUserId(userId);
        UUID staffId = staff.getId();

        long total     = bookingRepository.countByStaff_Id(staffId);
        long completed = bookingRepository.countByStaff_IdAndStatus(staffId, BookingStatus.COMPLETED);
        long pending   = bookingRepository.countByStaff_IdAndStatus(staffId, BookingStatus.PENDING)
                       + bookingRepository.countByStaff_IdAndStatus(staffId, BookingStatus.CONFIRMED);
        long cancelled = bookingRepository.countByStaff_IdAndStatus(staffId, BookingStatus.CANCELLED_BY_CUSTOMER)
                       + bookingRepository.countByStaff_IdAndStatus(staffId, BookingStatus.CANCELLED_BY_BUSINESS);
        double avgRating  = reviewRepository.avgStarsByStaffId(staffId);
        long reviewCount  = reviewRepository.findByStaff_Id(staffId).size();

        return StaffStatsDto.builder()
                .totalBookings(total)
                .completedBookings(completed)
                .pendingBookings(pending)
                .cancelledBookings(cancelled)
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }

    public StaffMemberDto create(UUID businessId, StaffMemberCreateRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        StaffMember entity = mapper.toEntity(request);
        entity.setBusiness(businessService.requireActiveAccess(businessId));
        applyServices(entity, businessId, request.getServiceIds());
        if (request.getLinkedUserId() != null) {
            requireNotAlreadyStaff(businessId, request.getLinkedUserId());
            AppUser user = userService.requireUser(request.getLinkedUserId());
            entity.setLinkedUser(user);
            grantStaffRole(user);
        }
        return toDtoWithStats(repository.save(entity));
    }

    /**
     * Xodim uchun bir vaqtning o'zida yangi foydalanuvchi hisobi (login/parol) yaratadi
     * va uni shu xodim yozuviga bog'laydi — xodim keyin shu login bilan tizimga kirib,
     * o'z portalidan foydalana oladi.
     */
    public StaffMemberDto registerStaff(UUID businessId, StaffRegisterRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        Business business = businessService.requireActiveAccess(businessId);
        AppUser newUser = userService.createAccountForStaff(
                request.getLogin(), request.getPassword(), request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getPhone());
        grantStaffRole(newUser);

        StaffMember entity = new StaffMember();
        entity.setBusiness(business);
        entity.setFirstName(request.getFirstName().trim());
        entity.setLastName(request.getLastName() == null || request.getLastName().isBlank() ? null : request.getLastName().trim());
        entity.setAvatarUrl(request.getAvatarUrl());
        entity.setBio(request.getBio());
        entity.setExperienceYears(request.getExperienceYears());
        entity.setLinkedUser(newUser);
        entity.setActive(true);
        applyServices(entity, businessId, request.getServiceIds());
        return toDtoWithStats(repository.save(entity));
    }

    /**
     * Avval "hisobsiz" yaratilgan xodimga keyinroq yangi foydalanuvchi hisobi
     * (login/parol) ochib beradi va shu xodim yozuviga bog'laydi.
     */
    public StaffMemberDto registerAccountForExisting(UUID businessId, UUID staffId, StaffRegisterRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);
        if (entity.getLinkedUser() != null) {
            throw new IllegalStateException("Bu xodim allaqachon foydalanuvchi hisobiga bog'langan");
        }
        AppUser newUser = userService.createAccountForStaff(
                request.getLogin(), request.getPassword(), request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getPhone());
        grantStaffRole(newUser);
        entity.setLinkedUser(newUser);
        entity.setFirstName(request.getFirstName().trim());
        entity.setLastName(request.getLastName() == null || request.getLastName().isBlank() ? null : request.getLastName().trim());
        entity.setAvatarUrl(request.getAvatarUrl());
        entity.setBio(request.getBio());
        entity.setExperienceYears(request.getExperienceYears());
        applyServices(entity, businessId, request.getServiceIds());
        return toDtoWithStats(entity);
    }
    /**
     * Xodimga biriktirilgan hisobning joriy login/email/telefon ma'lumotlarini qaytaradi —
     * tahrirlash formasini oldindan to'ldirish uchun. Faqat shu biznes egasi/admin ko'ra oladi.
     */
    @Transactional(readOnly = true)
    public UserLookupDto getLinkedAccountInfo(UUID businessId, UUID staffId) {
        businessService.requireManagerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);
        AppUser linkedUser = entity.getLinkedUser();
        if (linkedUser == null) {
            throw new IllegalStateException("Bu xodim hech qanday hisobga bog'lanmagan");
        }
        return UserLookupDto.builder()
                .id(linkedUser.getId())
                .login(linkedUser.getUsername())
                .firstName(linkedUser.getFirstName())
                .lastName(linkedUser.getLastName())
                .email(linkedUser.getEmail())
                .phone(linkedUser.getPhone())
                .build();
    }

    public StaffMemberDto updateLinkedAccount(UUID businessId, UUID staffId, StaffAccountUpdateRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);
        AppUser linkedUser = entity.getLinkedUser();
        if (linkedUser == null) {
            throw new IllegalStateException("Bu xodim hech qanday hisobga bog'lanmagan");
        }
        userService.updateStaffAccountFields(
                linkedUser, request.getFirstName(), request.getLastName(), request.getEmail(), request.getPhone(), request.getPassword());
        if (request.getFirstName() != null || request.getLastName() != null) {
            if (request.getFirstName() != null) {
                entity.setFirstName(request.getFirstName().trim());
            }
            if (request.getLastName() != null) {
                entity.setLastName(request.getLastName().isBlank() ? null : request.getLastName().trim());
            }
        }
        return toDtoWithStats(entity);
    }

    public StaffMemberDto update(UUID businessId, UUID staffId, StaffMemberUpdateRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);

        boolean isNewLink = request.getLinkedUserId() != null
                && (entity.getLinkedUser() == null || !entity.getLinkedUser().getId().equals(request.getLinkedUserId()));
        if (isNewLink) {
            requireNotAlreadyStaff(businessId, request.getLinkedUserId());
            if (entity.getLinkedUser() != null) {
                revokeStaffRole(entity.getLinkedUser());
            }
        }

        mapper.update(entity, request);
        if (request.getServiceIds() != null) {
            applyServices(entity, businessId, request.getServiceIds());
        }

        if (isNewLink) {
            AppUser newUser = userService.requireUser(request.getLinkedUserId());
            entity.setLinkedUser(newUser);
            grantStaffRole(newUser);
        }
        return toDtoWithStats(entity);
    }

    /** Bu foydalanuvchi shu biznesda allaqachon boshqa xodim yozuviga bog'langan bo'lsa, xatolik qaytaradi. */
    private void requireNotAlreadyStaff(UUID businessId, UUID linkedUserId) {
        if (repository.existsByBusiness_IdAndLinkedUser_Id(businessId, linkedUserId)) {
            throw new IllegalArgumentException("Bu foydalanuvchi bu biznesda allaqachon xodim sifatida ro'yxatga olingan");
        }
    }

    public void delete(UUID businessId, UUID staffId) {
        businessService.requireManagerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);
        // Bog'langan foydalanuvchidan ROLE_STAFF ni olib tashlash
        if (entity.getLinkedUser() != null) {
            revokeStaffRole(entity.getLinkedUser());
        }
        repository.delete(entity);
    }

    /** Joriy foydalanuvchi berilgan xodim yozuviga bog'langanmi (bron ruxsatlarini tekshirish uchun). */
    @Transactional(readOnly = true)
    public boolean isCurrentUserLinkedTo(UUID staffId) {
        if (staffId == null) return false;
        UUID currentUserId = currentUserService.getCurrentUserId();
        return repository.findByLinkedUser_Id(currentUserId)
                .map(sm -> sm.getId().equals(staffId))
                .orElse(false);
    }

    /** Joriy foydalanuvchi berilgan biznesning (istalgan) xodimimi — bron yaratish huquqini tekshirish uchun. */
    @Transactional(readOnly = true)
    public boolean isCurrentUserStaffOfBusiness(UUID businessId) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return repository.findByLinkedUser_Id(currentUserId)
                .map(sm -> sm.getBusiness().getId().equals(businessId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canPerformService(UUID businessId, UUID staffId, UUID serviceId) {
        return repository.existsByBusiness_IdAndIdAndOfferedServices_Id(businessId, staffId, serviceId);
    }

    StaffMember requireStaff(UUID businessId, UUID staffId) {
        return repository.findByBusiness_IdAndId(businessId, staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Xodim topilmadi: " + staffId));
    }

    private StaffMember requireStaffByUserId(UUID userId) {
        return repository.findByLinkedUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Xodim profili topilmadi"));
    }

    private void grantStaffRole(AppUser user) {
        roleRepository.assignRoleIfPresent(user, "ROLE_STAFF");
    }

    private void revokeStaffRole(AppUser user) {
        roleRepository.removeRoleIfPresent(user, "ROLE_STAFF");
    }

    private void applyServices(StaffMember entity, UUID businessId, Set<UUID> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            entity.getOfferedServices().clear();
            return;
        }
        List<OfferedService> services = offeredServiceRepository.findByBusiness_IdAndIdIn(businessId, serviceIds);
        if (services.size() != serviceIds.size()) {
            throw new ResourceNotFoundException("Xodimga biriktiriladigan xizmatlardan biri topilmadi");
        }
        entity.setOfferedServices(new HashSet<>(services));
    }

    private StaffMemberDto toDtoWithStats(StaffMember entity) {
        StaffMemberDto dto = mapper.toDto(entity);
        UUID staffId = entity.getId();
        dto.setAvgRating(reviewRepository.avgStarsByStaffId(staffId));
        dto.setReviewCount(reviewRepository.countByStaff_Id(staffId));
        return dto;
    }
}

