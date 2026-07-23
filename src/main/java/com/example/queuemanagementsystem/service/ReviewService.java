package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.domain.Review;
import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.dto.ReviewCreateRequest;
import com.example.queuemanagementsystem.dto.ReviewDto;
import com.example.queuemanagementsystem.dto.ReviewUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.ReviewMapper;
import com.example.queuemanagementsystem.repository.ReviewRepository;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final BookingService bookingService;
    private final StaffMemberRepository staffMemberRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ReviewDto> findAll(UUID businessId, UUID staffId) {
        if (staffId != null) {
            return repository.findByStaff_Id(staffId).stream().map(mapper::toDto).toList();
        }
        if (businessId != null) {
            return repository.findByBooking_Business_Id(businessId).stream().map(mapper::toDto).toList();
        }
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public double avgRating(UUID staffId) {
        return repository.avgStarsByStaffId(staffId);
    }

    @Transactional(readOnly = true)
    public ReviewDto get(UUID id) {
        return mapper.toDto(requireReview(id));
    }

    public ReviewDto create(ReviewCreateRequest request) {
        Booking booking = bookingService.requireBooking(request.getBookingId());
        boolean isBookingCustomer = booking.getCustomer() != null
                && booking.getCustomer().getId().equals(currentUserService.getCurrentUserId());
        if (!currentUserService.isAdmin() && !isBookingCustomer) {
            throw new AccessDeniedException("Faqat o'z bronigiz uchun sharh qoldira olasiz");
        }
        if (booking.getStatus() != com.example.queuemanagementsystem.domain.enums.BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Faqat yakunlangan bronlar uchun sharh qoldirish mumkin");
        }
        Review entity = mapper.toEntity(request);
        entity.setBooking(booking);
        // Bookingdagi xodimni avtomatik bog'lash
        if (booking.getStaff() != null) {
            entity.setStaff(booking.getStaff());
        }
        return mapper.toDto(repository.save(entity));
    }

    public ReviewDto update(UUID id, ReviewUpdateRequest request) {
        Review entity = requireReview(id);
        requireReviewerOrAdmin(entity);
        mapper.update(entity, request);
        return mapper.toDto(entity);
    }

    public void delete(UUID id) {
        Review entity = requireReview(id);
        requireReviewerOrAdmin(entity);
        repository.deleteById(id);
    }

    Review requireReview(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sharh topilmadi: " + id));
    }

    private void requireReviewerOrAdmin(Review review) {
        if (currentUserService.isAdmin()) return;
        UUID currentId = currentUserService.getCurrentUserId();
        var customer = review.getBooking().getCustomer();
        if (customer == null || !customer.getId().equals(currentId)) {
            throw new AccessDeniedException("Bu sharhga ruxsat yo'q");
        }
    }
}
