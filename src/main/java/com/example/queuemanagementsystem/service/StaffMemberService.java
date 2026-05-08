package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import com.example.queuemanagementsystem.dto.BookingDto;
import com.example.queuemanagementsystem.dto.StaffMemberCreateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffMemberUpdateRequest;
import com.example.queuemanagementsystem.dto.StaffStatsDto;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.BookingMapper;
import com.example.queuemanagementsystem.mapper.StaffMemberMapper;
import com.example.queuemanagementsystem.repository.BookingRepository;
import com.example.queuemanagementsystem.repository.ReviewRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<StaffMemberDto> findAll(UUID businessId) {
        businessService.requireBusiness(businessId);
        return repository.findByBusiness_Id(businessId).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public StaffMemberDto get(UUID businessId, UUID staffId) {
        return mapper.toDto(requireStaff(businessId, staffId));
    }

    /** Joriy foydalanuvchining xodim profilini qaytaradi */
    @Transactional(readOnly = true)
    public StaffMemberDto getMyProfile() {
        UUID userId = currentUserService.requireUserId();
        return mapper.toDto(requireStaffByUserId(userId));
    }

    /** Joriy xodimga biriktirilgan bronlarni qaytaradi */
    @Transactional(readOnly = true)
    public List<BookingDto> getMyBookings() {
        UUID userId = currentUserService.requireUserId();
        StaffMember staff = requireStaffByUserId(userId);
        return bookingRepository.findByStaff_Id(staff.getId())
                .stream().map(bookingMapper::toDto).toList();
    }

    /** Joriy xodimning statistikasini qaytaradi */
    @Transactional(readOnly = true)
    public StaffStatsDto getMyStats() {
        UUID userId = currentUserService.requireUserId();
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
        StaffMember entity = mapper.toEntity(request);
        entity.setBusiness(businessService.requireActiveAccess(businessId));
        if (request.getLinkedUserId() != null) {
            AppUser user = userService.requireUser(request.getLinkedUserId());
            entity.setLinkedUser(user);
            grantStaffRole(user);
        }
        return mapper.toDto(repository.save(entity));
    }

    public StaffMemberDto update(UUID businessId, UUID staffId, StaffMemberUpdateRequest request) {
        businessService.requireOwnerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);

        // Eski linked user dan ROLE_STAFF ni olib tashlash
        if (request.getLinkedUserId() != null) {
            if (entity.getLinkedUser() != null) {
                revokeStaffRole(entity.getLinkedUser());
            }
        }

        mapper.update(entity, request);

        if (request.getLinkedUserId() != null) {
            AppUser newUser = userService.requireUser(request.getLinkedUserId());
            entity.setLinkedUser(newUser);
            grantStaffRole(newUser);
        }
        return mapper.toDto(entity);
    }

    public void delete(UUID businessId, UUID staffId) {
        businessService.requireOwnerOrAdmin(businessId);
        StaffMember entity = requireStaff(businessId, staffId);
        // Bog'langan foydalanuvchidan ROLE_STAFF ni olib tashlash
        if (entity.getLinkedUser() != null) {
            revokeStaffRole(entity.getLinkedUser());
        }
        repository.delete(entity);
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
        Role staffRole = roleRepository.findByName("ROLE_STAFF");
        if (staffRole != null) {
            user.getRoles().add(staffRole);
        }
    }

    private void revokeStaffRole(AppUser user) {
        Role staffRole = roleRepository.findByName("ROLE_STAFF");
        if (staffRole != null) {
            user.getRoles().remove(staffRole);
        }
    }
}
