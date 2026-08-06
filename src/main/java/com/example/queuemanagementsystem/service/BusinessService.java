package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.enums.BusinessCategory;
import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import com.example.queuemanagementsystem.domain.enums.ReviewAction;
import com.example.queuemanagementsystem.dto.BusinessCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessDto;
import com.example.queuemanagementsystem.dto.BusinessReviewRequest;
import com.example.queuemanagementsystem.dto.BusinessStatusRequest;
import com.example.queuemanagementsystem.dto.BusinessUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.exception.BusinessAccessDeniedException;
import com.example.queuemanagementsystem.mapper.BusinessMapper;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessService {

    private final BusinessRepository repository;
    private final BusinessMapper mapper;
    private final AppUserService userService;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<BusinessDto> findAll(UUID ownerId, BusinessCategory category, BusinessStatus status, String city, String q, Pageable pageable) {
        String normalizedCity = normalizeFilter(city);
        String normalizedQ = normalizeFilter(q);
        String sortProperty = pageable.getSort().stream()
                .findFirst()
                .map(Sort.Order::getProperty)
                .orElse("createdAt");
        if ("rating".equals(sortProperty)) {
            Pageable aggregatePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            return repository.searchOrderByRating(ownerId, category, status, normalizedCity, normalizedQ, aggregatePageable).map(mapper::toDto);
        }
        if ("reviews".equals(sortProperty)) {
            Pageable aggregatePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            return repository.searchOrderByReviewCount(ownerId, category, status, normalizedCity, normalizedQ, aggregatePageable).map(mapper::toDto);
        }
        return repository.search(ownerId, category, status, normalizedCity, normalizedQ, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<String> findCities() {
        return repository.findDistinctCities();
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    @Transactional(readOnly = true)
    public BusinessDto get(UUID id) {
        return mapper.toDto(requireBusiness(id));
    }

    public BusinessDto create(BusinessCreateRequest request) {
        UUID ownerId = request.getOwnerId();
        if (!currentUserService.isAdmin()) {
            ownerId = currentUserService.getCurrentUserId();
        }
        AppUser owner = userService.requireUser(ownerId);
        // Owner hali ROLE_BUSINESS_OWNER roliga ega bo'lmasa, avtomatik beriladi (Set — takror qo'shilmaydi)
        roleRepository.assignRoleIfPresent(owner, "ROLE_BUSINESS_OWNER");

        Business entity = mapper.toEntity(request);
        entity.setOwner(owner);
        if (request.getCategory() == null) {
            entity.setCategory(BusinessCategory.OTHER);
        }
        // Yangi biznes har doim 14 kunlik TRIAL bilan boshlanadi
        entity.setStatus(BusinessStatus.TRIAL);
        entity.setTrialEndDate(Instant.now().plus(14, ChronoUnit.DAYS));
        return mapper.toDto(repository.save(entity));
    }

    public BusinessDto update(UUID id, BusinessUpdateRequest request) {
        Business entity = requireBusiness(id);
        requireOwnerOrAdmin(entity);
        mapper.update(entity, request);
        return mapper.toDto(entity);
    }

    public BusinessDto changeStatus(UUID id, BusinessStatusRequest request) {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Faqat admin status o'zgartira oladi");
        }
        Business entity = requireBusiness(id);
        String oldStatus = entity.getStatus().name();
        if (request.getStatus() == BusinessStatus.ACTIVE
                && request.getSubscriptionEndDate() != null
                && !request.getSubscriptionEndDate().isAfter(Instant.now())) {
            throw new IllegalArgumentException("ACTIVE holati uchun obuna tugash sanasi kelajakda bo'lishi kerak yoki bo'sh qoldirilishi kerak");
        }
        entity.setStatus(request.getStatus());
        entity.setSubscriptionEndDate(request.getSubscriptionEndDate());
        BusinessDto result = mapper.toDto(repository.save(entity));
        auditLogService.log(
                AuditLogService.BUSINESS_STATUS_CHANGED, "BUSINESS", id.toString(),
                oldStatus + " → " + request.getStatus().name());
        return result;
    }

    public BusinessDto review(UUID id, BusinessReviewRequest request) {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Faqat admin ko'rib chiqishi mumkin");
        }
        Business entity = requireBusiness(id);
        if (entity.getStatus() != BusinessStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Biznes PENDING_REVIEW holatida emas");
        }

        String adminLogin = currentUserService.getCurrentUsername();
        if (adminLogin == null) adminLogin = "system";

        if (request.getAction() == ReviewAction.APPROVE) {
            entity.setStatus(BusinessStatus.ACTIVE);
            if (request.getSubscriptionEndDate() != null) {
                entity.setSubscriptionEndDate(request.getSubscriptionEndDate());
            }
        } else {
            entity.setStatus(BusinessStatus.DRAFT);
        }

        entity.setReviewNote(request.getNote());
        entity.setReviewedBy(adminLogin);
        entity.setReviewedAt(Instant.now());

        BusinessDto result = mapper.toDto(repository.save(entity));
        auditLogService.log(
                AuditLogService.BUSINESS_REVIEWED, "BUSINESS", id.toString(),
                request.getAction().name() + (request.getNote() != null ? ": " + request.getNote() : ""));
        return result;
    }

    public void delete(UUID id) {
        Business entity = requireBusiness(id);
        requireOwnerOrAdmin(entity);
        repository.deleteById(id);
        auditLogService.log(AuditLogService.BUSINESS_DELETED, "BUSINESS", id.toString(),
                "Biznes o'chirildi: " + entity.getName());
    }

    Business requireBusiness(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biznes topilmadi: " + id));
    }

    public void requireOwnerOrAdmin(UUID businessId) {
        requireOwnerOrAdmin(requireBusiness(businessId));
    }

    /** requireOwnerOrAdmin bilan bir xil tekshiruv, lekin istisno otmasdan boolean qaytaradi. */
    public boolean isOwnerOrAdmin(UUID businessId) {
        return isOwnerOrAdmin(requireBusiness(businessId));
    }

    private boolean isOwnerOrAdmin(Business entity) {
        return currentUserService.isAdmin()
                || entity.getOwner().getId().equals(currentUserService.getCurrentUserId());
    }

    private void requireOwnerOrAdmin(Business entity) {
        if (!isOwnerOrAdmin(entity)) {
            throw new AccessDeniedException("Bu biznesga ruxsat yo'q");
        }
    }

    /**
     * Biznesni topadi va uning obuna/trial ruxsati faolligini tekshiradi.
     * EXPIRED yoki SUSPENDED bo'lsa {@link BusinessAccessDeniedException} otadi.
     */
    public Business requireActiveAccess(UUID id) {
        Business business = requireBusiness(id);
        if (!business.isAccessAllowed()) {
            throw new BusinessAccessDeniedException(
                    "Biznesning sinov muddati tugagan yoki obuna faol emas. " +
                    "Davom etish uchun obuna sotib oling."
            );
        }
        return business;
    }
}
