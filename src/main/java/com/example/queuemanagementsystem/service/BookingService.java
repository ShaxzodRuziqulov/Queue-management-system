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
import com.example.queuemanagementsystem.repository.ReviewRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
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
    private final ReviewRepository reviewRepository;
    private final CurrentUserService currentUserService;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public Page<BookingDto> findAll(UUID customerId, UUID businessId, LocalDate date, Pageable pageable) {
        if (customerId != null) {
            if (!currentUserService.isAdmin() && !customerId.equals(currentUserService.getCurrentUserId())) {
                throw new AccessDeniedException("Boshqa mijozning bronlarini ko'rish mumkin emas");
            }
            userService.requireUser(customerId);
            return repository.findByCustomer_Id(customerId, pageable).map(mapper::toDto);
        }
        if (businessId != null) {
            businessService.requireOwnerOrAdmin(businessId);
            if (date != null) {
                Instant dayStart = date.atStartOfDay(BUSINESS_ZONE).toInstant();
                Instant dayEnd = date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
                return repository.findByBusiness_IdAndStartAtBetween(businessId, dayStart, dayEnd, pageable).map(mapper::toDto);
            }
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
        boolean isBusinessManager = businessService.isOwnerOrAdmin(request.getBusinessId());
        boolean isBusinessStaff = staffMemberService.isCurrentUserStaffOfBusiness(request.getBusinessId());
        boolean isSelfBooking = request.getCustomerId() != null
                && request.getCustomerId().equals(currentUserService.getCurrentUserId());
        if (!currentUserService.isAdmin() && !isBusinessManager && !isBusinessStaff && !isSelfBooking) {
            throw new AccessDeniedException("Faqat biznes egasi, xodimi yoki o'zingiz uchun bron yarata olasiz");
        }
        OfferedService offeredService = offeredServiceService.requireOfferedService(
                request.getBusinessId(), request.getOfferedServiceId());
        Booking entity = mapper.toEntity(request);
        if (request.getCustomerId() != null) {
            entity.setCustomer(userService.requireUser(request.getCustomerId()));
        } else if (!StringUtils.hasText(request.getGuestName())) {
            throw new IllegalArgumentException("Mijoz ismini kiriting");
        }
        // Biznes trial/obuna faolligini tekshirish
        Business business = businessService.requireActiveAccess(request.getBusinessId());
        entity.setBusiness(business);
        entity.setOfferedService(offeredService);
        // Mijoz o'zi band qilganda boshlang'ich holat har doim PENDING (holatni o'zi belgilab bo'lmaydi).
        // Biznes egasi/xodimi/admin o'zi bron yaratsa — bu allaqachon tasdiqlangan hisoblanadi,
        // qayta "tasdiqlash" bosishga majburlanmasin (agar so'rovda aniq holat ko'rsatilmagan bo'lsa).
        boolean isTrustedActor = currentUserService.isAdmin() || isBusinessManager || isBusinessStaff;
        if (!isTrustedActor) {
            entity.setStatus(BookingStatus.PENDING);
        } else if (entity.getStatus() == null) {
            entity.setStatus(BookingStatus.CONFIRMED);
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
        // Mijozlar bazasini avtomatik to'ldirish: telefonli mehmon bronlar biznesning
        // mijoz profiliga yig'iladi (topiladi yoki yaratiladi, tashrif soni oshadi).
        if (StringUtils.hasText(request.getGuestPhone())) {
            entity.setClient(customerService.upsertFromBooking(
                    business, request.getGuestName(), request.getGuestPhone()));
        }
        return mapper.toDto(repository.save(entity));
    }

    public BookingDto update(UUID id, BookingUpdateRequest request) {
        Booking entity = requireBooking(id);
        // Read-only rejim: muddati tugagan biznesda bronni o'zgartirib bo'lmaydi (admin — istisno).
        if (!currentUserService.isAdmin()) {
            businessService.requireActiveAccess(entity.getBusiness().getId());
        }
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
        // Read-only rejim: muddati tugagan biznesda bronni o'chirib bo'lmaydi (admin — istisno).
        if (!currentUserService.isAdmin()) {
            businessService.requireActiveAccess(entity.getBusiness().getId());
        }
        requireWriteAccess(entity, null);
        if (reviewRepository.existsByBooking_Id(id)) {
            throw new IllegalStateException("Bu bronga sharh bog'langan. Avval sharhni o'chiring");
        }
        repository.deleteById(id);
    }

    Booking requireBooking(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bron topilmadi: " + id));
    }

    private boolean isParticipant(Booking booking) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(currentUserId);
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
        boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(currentUserId);
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
