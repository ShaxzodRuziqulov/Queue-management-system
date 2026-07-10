package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.BusinessHours;
import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import com.example.queuemanagementsystem.domain.enums.Weekday;
import com.example.queuemanagementsystem.dto.BookingCreateRequest;
import com.example.queuemanagementsystem.dto.BookingDto;
import com.example.queuemanagementsystem.dto.BookingUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.BookingMapper;
import com.example.queuemanagementsystem.repository.BookingRepository;
import com.example.queuemanagementsystem.repository.BusinessHoursRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    /** Bookinglar biznesning mahalliy vaqti bo'yicha tekshiriladi (ish vaqti). */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Tashkent");

    private static final Set<BookingStatus> NON_BLOCKING_STATUSES = Set.of(
            BookingStatus.CANCELLED_BY_CUSTOMER,
            BookingStatus.CANCELLED_BY_BUSINESS,
            BookingStatus.NO_SHOW
    );

    /** Ruxsat etilgan holat o'tishlari (state machine). Terminal holatlardan chiqish yo'q. */
    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatus.PENDING, Set.of(
                    BookingStatus.CONFIRMED, BookingStatus.CANCELLED_BY_CUSTOMER, BookingStatus.CANCELLED_BY_BUSINESS),
            BookingStatus.CONFIRMED, Set.of(
                    BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED_BY_CUSTOMER,
                    BookingStatus.CANCELLED_BY_BUSINESS, BookingStatus.NO_SHOW),
            BookingStatus.IN_PROGRESS, Set.of(BookingStatus.COMPLETED, BookingStatus.NO_SHOW),
            BookingStatus.COMPLETED, Set.of(),
            BookingStatus.CANCELLED_BY_CUSTOMER, Set.of(),
            BookingStatus.CANCELLED_BY_BUSINESS, Set.of(),
            BookingStatus.NO_SHOW, Set.of()
    );

    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final AppUserService userService;
    private final BusinessService businessService;
    private final OfferedServiceService offeredServiceService;
    private final StaffMemberService staffMemberService;
    private final BusinessHoursRepository businessHoursRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<BookingDto> findAll(UUID customerId, UUID businessId, Pageable pageable) {
        if (customerId != null) {
            if (!currentUserService.isAdmin() && !customerId.equals(currentUserService.getCurrentUserId())) {
                throw new AccessDeniedException("Boshqa mijozning bronlarini ko'rish mumkin emas");
            }
            userService.requireUser(customerId);
            return repository.findByCustomer_Id(customerId, pageable).map(mapper::toDto);
        }
        if (businessId != null) {
            businessService.requireOwnerOrAdmin(businessId);
            return repository.findByBusiness_Id(businessId, pageable).map(mapper::toDto);
        }
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Bronlar ro'yxatini ko'rish uchun customerId yoki businessId filtri talab qilinadi");
        }
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public BookingDto get(UUID id) {
        Booking booking = requireBooking(id);
        if (!currentUserService.isAdmin() && !isParticipant(booking)) {
            throw new AccessDeniedException("Bu bronga ruxsat yo'q");
        }
        return mapper.toDto(booking);
    }

    public BookingDto create(BookingCreateRequest request) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException("Tugash vaqti boshlanishdan keyin bo'lishi kerak");
        }
        UUID customerId = request.getCustomerId();
        if (!currentUserService.isAdmin()) {
            customerId = currentUserService.getCurrentUserId();
        }
        OfferedService offeredService = offeredServiceService.requireOfferedService(
                request.getBusinessId(), request.getOfferedServiceId());
        Booking entity = mapper.toEntity(request);
        entity.setCustomer(userService.requireUser(customerId));
        // Biznes trial/obuna faolligini tekshirish
        Business business = businessService.requireActiveAccess(request.getBusinessId());
        entity.setBusiness(business);
        entity.setOfferedService(offeredService);
        if (entity.getStatus() == null) {
            entity.setStatus(BookingStatus.PENDING);
        }
        StaffMember staff = null;
        if (request.getStaffId() != null) {
            staff = staffMemberService.requireStaff(request.getBusinessId(), request.getStaffId());
            entity.setStaff(staff);
        }
        checkBusinessHours(business.getId(), request.getStartAt(), request.getEndAt());
        if (staff != null) {
            checkNoOverlap(staff.getId(), request.getStartAt(), request.getEndAt(), null);
        }
        return mapper.toDto(repository.save(entity));
    }

    public BookingDto update(UUID id, BookingUpdateRequest request) {
        Booking entity = requireBooking(id);
        requireWriteAccess(entity, request.getStatus());
        mapper.update(entity, request);
        if (request.getStaffId() != null) {
            entity.setStaff(staffMemberService.requireStaff(entity.getBusiness().getId(), request.getStaffId()));
        }
        if (!entity.getEndAt().isAfter(entity.getStartAt())) {
            throw new IllegalArgumentException("Tugash vaqti boshlanishdan keyin bo'lishi kerak");
        }
        if (request.getStaffId() != null || request.getStartAt() != null || request.getEndAt() != null) {
            checkBusinessHours(entity.getBusiness().getId(), entity.getStartAt(), entity.getEndAt());
            if (entity.getStaff() != null) {
                checkNoOverlap(entity.getStaff().getId(), entity.getStartAt(), entity.getEndAt(), entity.getId());
            }
        }
        return mapper.toDto(entity);
    }

    public void delete(UUID id) {
        Booking entity = requireBooking(id);
        requireWriteAccess(entity, null);
        repository.deleteById(id);
    }

    Booking requireBooking(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bron topilmadi: " + id));
    }

    private boolean isParticipant(Booking booking) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        boolean isCustomer = booking.getCustomer().getId().equals(currentUserId);
        boolean isBusinessManager = businessService.isOwnerOrAdmin(booking.getBusiness().getId());
        boolean isAssignedStaff = booking.getStaff() != null
                && staffMemberService.isCurrentUserLinkedTo(booking.getStaff().getId());
        return isCustomer || isBusinessManager || isAssignedStaff;
    }

    /**
     * Bronni yozish (update/delete/status o'zgartirish) huquqini tekshiradi.
     * Admin — istalgan holat. Biznes egasi/menejeri yoki biriktirilgan xodim — barcha o'tishlar
     * (state machine chegarasida). Mijoz — faqat o'z bronini bekor qila oladi.
     */
    private void requireWriteAccess(Booking booking, BookingStatus newStatus) {
        if (currentUserService.isAdmin()) {
            if (newStatus != null) validateTransition(booking.getStatus(), newStatus);
            return;
        }
        UUID currentUserId = currentUserService.getCurrentUserId();
        boolean isCustomer = booking.getCustomer().getId().equals(currentUserId);
        boolean isBusinessManager = businessService.isOwnerOrAdmin(booking.getBusiness().getId());
        boolean isAssignedStaff = booking.getStaff() != null
                && staffMemberService.isCurrentUserLinkedTo(booking.getStaff().getId());

        if (!isCustomer && !isBusinessManager && !isAssignedStaff) {
            throw new AccessDeniedException("Bu bronga ruxsat yo'q");
        }
        if (newStatus != null && newStatus != booking.getStatus()) {
            if (isCustomer && !isBusinessManager && !isAssignedStaff && newStatus != BookingStatus.CANCELLED_BY_CUSTOMER) {
                throw new AccessDeniedException("Mijoz faqat bronni bekor qila oladi");
            }
            validateTransition(booking.getStatus(), newStatus);
        }
    }

    private void validateTransition(BookingStatus from, BookingStatus to) {
        if (from == to) return;
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalStateException("Bron holatini " + from + " dan " + to + " ga o'zgartirib bo'lmaydi");
        }
    }

    private void checkNoOverlap(UUID staffId, Instant startAt, Instant endAt, UUID excludeBookingId) {
        boolean overlaps = repository.existsOverlapping(staffId, startAt, endAt, excludeBookingId, NON_BLOCKING_STATUSES);
        if (overlaps) {
            throw new IllegalArgumentException("Bu xodim uchun tanlangan vaqtda boshqa bron allaqachon mavjud");
        }
    }

    private void checkBusinessHours(UUID businessId, Instant startAt, Instant endAt) {
        ZonedDateTime zonedStart = startAt.atZone(BUSINESS_ZONE);
        Weekday weekday = Weekday.valueOf(zonedStart.getDayOfWeek().name());
        BusinessHours hours = businessHoursRepository.findByBusiness_IdAndWeekday(businessId, weekday).orElse(null);
        if (hours == null) {
            return; // Ish vaqti belgilanmagan bo'lsa, cheklov qo'yilmaydi
        }
        if (hours.isClosed()) {
            throw new IllegalArgumentException("Bu kunda biznes ishlamaydi");
        }
        LocalTime start = zonedStart.toLocalTime();
        LocalTime end = endAt.atZone(BUSINESS_ZONE).toLocalTime();
        if (hours.getOpensAt() != null && start.isBefore(hours.getOpensAt())) {
            throw new IllegalArgumentException("Band qilish ish vaqtidan oldin boshlanadi");
        }
        if (hours.getClosesAt() != null && end.isAfter(hours.getClosesAt())) {
            throw new IllegalArgumentException("Band qilish ish vaqtidan keyin tugaydi");
        }
    }
}
