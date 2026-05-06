package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import com.example.queuemanagementsystem.dto.BusinessCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessDto;
import com.example.queuemanagementsystem.dto.BusinessStatusRequest;
import com.example.queuemanagementsystem.dto.BusinessUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.exception.BusinessAccessDeniedException;
import com.example.queuemanagementsystem.mapper.BusinessMapper;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public List<BusinessDto> findAll(UUID ownerId) {
        if (ownerId != null) {
            userService.requireUser(ownerId);
            return repository.findByOwner_Id(ownerId).stream().map(mapper::toDto).toList();
        }
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BusinessDto get(UUID id) {
        return mapper.toDto(requireBusiness(id));
    }

    public BusinessDto create(BusinessCreateRequest request) {
        UUID ownerId = request.getOwnerId();
        if (!currentUserService.isAdmin()) {
            ownerId = currentUserService.requireUserId();
        }
        Business entity = mapper.toEntity(request);
        entity.setOwner(userService.requireUser(ownerId));
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

    public void delete(UUID id) {
        Business entity = requireBusiness(id);
        requireOwnerOrAdmin(entity);
        repository.deleteById(id);
    }

    public BusinessDto changeStatus(UUID id, BusinessStatusRequest request) {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Faqat admin status o'zgartira oladi");
        }
        Business entity = requireBusiness(id);
        entity.setStatus(request.getStatus());
        entity.setSubscriptionEndDate(request.getSubscriptionEndDate());
        return mapper.toDto(repository.save(entity));
    }

    Business requireBusiness(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biznes topilmadi: " + id));
    }

    public void requireOwnerOrAdmin(UUID businessId) {
        if (currentUserService.isAdmin()) return;
        Business entity = requireBusiness(businessId);
        UUID currentUserId = currentUserService.requireUserId();
        if (!entity.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Bu biznesga ruxsat yo'q");
        }
    }

    private void requireOwnerOrAdmin(Business entity) {
        if (currentUserService.isAdmin()) return;
        UUID currentUserId = currentUserService.requireUserId();
        if (!entity.getOwner().getId().equals(currentUserId)) {
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
