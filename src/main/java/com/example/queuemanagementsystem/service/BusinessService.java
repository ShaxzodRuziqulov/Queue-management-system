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
import com.example.queuemanagementsystem.dto.PublicBusinessSummaryDto;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.exception.BusinessAccessDeniedException;
import com.example.queuemanagementsystem.mapper.BusinessMapper;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    private final StaffMemberRepository staffMemberRepository;

    @Transactional(readOnly = true)
    public Page<BusinessDto> findAll(UUID ownerId, BusinessCategory category, BusinessStatus status, String city, String q, Pageable pageable) {
        String normalizedCity = normalizeFilter(city);
        String normalizedQ = normalizeFilter(q);
        String sortProperty = pageable.getSort().stream()
                .findFirst()
                .map(Sort.Order::getProperty)
                .orElse("createdAt");
        if ("rating".equals(sortProperty)) {
            return repository.searchOrderByRating(ownerId, category, status, normalizedCity, normalizedQ, unsortedPageable(pageable))
                    .map(mapper::toDto);
        }
        if ("reviews".equals(sortProperty)) {
            return repository.searchOrderByReviewCount(ownerId, category, status, normalizedCity, normalizedQ, unsortedPageable(pageable))
                    .map(mapper::toDto);
        }
        return repository.findAll(businessFilter(ownerId, category, status, city, q), pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<String> findCities() {
        return repository.findDistinctCities();
    }

    @Transactional(readOnly = true)
    public List<String> findPublicCities() {
        return repository.findPublicCities(Instant.now());
    }

    @Transactional(readOnly = true)
    public Page<PublicBusinessSummaryDto> findPublic(BusinessCategory category, String city, String q, Pageable pageable) {
        String normalizedCity = normalizeFilter(city);
        String normalizedQ = normalizeFilter(q);
        String sortProperty = pageable.getSort().stream()
                .findFirst()
                .map(Sort.Order::getProperty)
                .orElse("rating");
        Instant now = Instant.now();
        if ("reviews".equals(sortProperty)) {
            return repository.searchPublicOrderByReviewCount(category, normalizedCity, normalizedQ, now, unsortedPageable(pageable));
        }
        if ("name".equals(sortProperty)) {
            return repository.searchPublicOrderByName(category, normalizedCity, normalizedQ, now, unsortedPageable(pageable));
        }
        return repository.searchPublicOrderByRating(category, normalizedCity, normalizedQ, now, unsortedPageable(pageable));
    }

    @Transactional(readOnly = true)
    public BusinessDto getMine() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return repository.findAll(businessFilter(currentUserId, null, null, null, null), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .or(() -> staffMemberRepository.findByLinkedUser_Id(currentUserId).map(staff -> staff.getBusiness()))
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Biznes topilmadi"));
    }

    @Transactional(readOnly = true)
    public Map<BusinessStatus, Long> countByStatus(UUID ownerId, BusinessCategory category, String city, String q) {
        Map<BusinessStatus, Long> counts = new EnumMap<>(BusinessStatus.class);
        for (BusinessStatus status : BusinessStatus.values()) {
            counts.put(status, 0L);
        }
        for (Object[] row : repository.countByStatus(ownerId, category, normalizeFilter(city), normalizeFilter(q))) {
            counts.put((BusinessStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    private Specification<Business> businessFilter(UUID ownerId, BusinessCategory category, BusinessStatus status, String city, String q) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (ownerId != null) {
                predicates.add(cb.equal(root.get("owner").get("id"), ownerId));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                var services = root.join("offeredServices", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("name")), pattern),
                        cb.like(cb.lower(root.<String>get("description")), pattern),
                        cb.like(cb.lower(root.<String>get("addressLine")), pattern),
                        cb.like(cb.lower(root.<String>get("city")), pattern),
                        cb.like(cb.lower(services.<String>get("name")), pattern),
                        cb.like(cb.lower(services.<String>get("description")), pattern)
                ));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private Pageable unsortedPageable(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
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
        requireManagerOrAdmin(entity);
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

    public void requireManagerOrAdmin(UUID businessId) {
        requireManagerOrAdmin(requireBusiness(businessId));
    }

    /** requireOwnerOrAdmin bilan bir xil tekshiruv, lekin istisno otmasdan boolean qaytaradi. */
    public boolean isOwnerOrAdmin(UUID businessId) {
        return isOwnerOrAdmin(requireBusiness(businessId));
    }

    public boolean isManagerOrAdmin(UUID businessId) {
        return isManagerOrAdmin(requireBusiness(businessId));
    }

    private boolean isOwnerOrAdmin(Business entity) {
        return currentUserService.isAdmin()
                || entity.getOwner().getId().equals(currentUserService.getCurrentUserId());
    }

    private boolean isManagerOrAdmin(Business entity) {
        if (isOwnerOrAdmin(entity)) {
            return true;
        }
        UUID currentUserId = currentUserService.getCurrentUserId();
        return currentUserService.hasRole("MANAGER")
                && staffMemberRepository.existsByBusiness_IdAndLinkedUser_Id(entity.getId(), currentUserId);
    }

    private void requireOwnerOrAdmin(Business entity) {
        if (!isOwnerOrAdmin(entity)) {
            throw new AccessDeniedException("Bu biznesga ruxsat yo'q");
        }
    }

    private void requireManagerOrAdmin(Business entity) {
        if (!isManagerOrAdmin(entity)) {
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
