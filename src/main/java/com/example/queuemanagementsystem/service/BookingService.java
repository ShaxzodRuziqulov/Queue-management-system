package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import com.example.queuemanagementsystem.dto.BookingCreateRequest;
import com.example.queuemanagementsystem.dto.BookingDto;
import com.example.queuemanagementsystem.dto.BookingUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.BookingMapper;
import com.example.queuemanagementsystem.repository.BookingRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final AppUserService userService;
    private final BusinessService businessService;
    private final OfferedServiceService offeredServiceService;
    private final StaffMemberService staffMemberService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<BookingDto> findAll(UUID customerId, UUID businessId) {
        if (customerId != null) {
            userService.requireUser(customerId);
            return repository.findByCustomer_Id(customerId).stream().map(mapper::toDto).toList();
        }
        if (businessId != null) {
            businessService.requireBusiness(businessId);
            return repository.findByBusiness_Id(businessId).stream().map(mapper::toDto).toList();
        }
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BookingDto get(UUID id) {
        return mapper.toDto(requireBooking(id));
    }

    public BookingDto create(BookingCreateRequest request) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException("Tugash vaqti boshlanishdan keyin bo‘lishi kerak");
        }
        UUID customerId = request.getCustomerId();
        if (!currentUserService.isAdmin()) {
            customerId = currentUserService.requireUserId();
        }
        OfferedService offeredService = offeredServiceService.requireOfferedService(
                request.getBusinessId(), request.getOfferedServiceId());
        Booking entity = mapper.toEntity(request);
        entity.setCustomer(userService.requireUser(customerId));
        entity.setBusiness(businessService.requireBusiness(request.getBusinessId()));
        entity.setOfferedService(offeredService);
        if (entity.getStatus() == null) {
            entity.setStatus(BookingStatus.PENDING);
        }
        if (request.getStaffId() != null) {
            StaffMember staff = staffMemberService.requireStaff(request.getBusinessId(), request.getStaffId());
            entity.setStaff(staff);
        }
        return mapper.toDto(repository.save(entity));
    }

    public BookingDto update(UUID id, BookingUpdateRequest request) {
        Booking entity = requireBooking(id);
        mapper.update(entity, request);
        if (request.getStaffId() != null) {
            entity.setStaff(staffMemberService.requireStaff(entity.getBusiness().getId(), request.getStaffId()));
        }
        if (!entity.getEndAt().isAfter(entity.getStartAt())) {
            throw new IllegalArgumentException("Tugash vaqti boshlanishdan keyin bo‘lishi kerak");
        }
        return mapper.toDto(entity);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Bron topilmadi: " + id);
        }
        repository.deleteById(id);
    }

    Booking requireBooking(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bron topilmadi: " + id));
    }
}
