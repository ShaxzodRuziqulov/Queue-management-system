package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.dto.OfferedServiceCreateRequest;
import com.example.queuemanagementsystem.dto.OfferedServiceDto;
import com.example.queuemanagementsystem.dto.OfferedServiceUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.OfferedServiceMapper;
import com.example.queuemanagementsystem.repository.OfferedServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferedServiceService {

    private final OfferedServiceRepository repository;
    private final OfferedServiceMapper mapper;
    private final BusinessService businessService;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<OfferedServiceDto> findAll(UUID businessId) {
        businessService.requireBusiness(businessId);
        return repository.findByBusiness_Id(businessId).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OfferedServiceDto get(UUID businessId, UUID serviceId) {
        return mapper.toDto(requireOfferedService(businessId, serviceId));
    }

    public OfferedServiceDto create(UUID businessId, OfferedServiceCreateRequest request) {
        OfferedService entity = mapper.toEntity(request);
        // Biznes trial/obuna faolligini tekshirish
        entity.setBusiness(businessService.requireActiveAccess(businessId));
        return mapper.toDto(repository.save(entity));
    }

    public OfferedServiceDto update(UUID businessId, UUID serviceId, OfferedServiceUpdateRequest request) {
        businessService.requireOwnerOrAdmin(businessId);
        OfferedService entity = requireOfferedService(businessId, serviceId);
        mapper.update(entity, request);
        return mapper.toDto(entity);
    }

    public void delete(UUID businessId, UUID serviceId) {
        businessService.requireOwnerOrAdmin(businessId);
        OfferedService entity = requireOfferedService(businessId, serviceId);
        if (entity.getImageUrl() != null) {
            fileStorageService.delete(entity.getImageUrl());
        }
        repository.delete(entity);
    }

    public OfferedServiceDto uploadImage(UUID businessId, UUID serviceId, MultipartFile file) {
        businessService.requireOwnerOrAdmin(businessId);
        OfferedService entity = requireOfferedService(businessId, serviceId);
        if (entity.getImageUrl() != null) {
            fileStorageService.delete(entity.getImageUrl());
        }
        String url = fileStorageService.store(file, "services");
        entity.setImageUrl(url);
        return mapper.toDto(entity);
    }

    public OfferedServiceDto deleteImage(UUID businessId, UUID serviceId) {
        businessService.requireOwnerOrAdmin(businessId);
        OfferedService entity = requireOfferedService(businessId, serviceId);
        if (entity.getImageUrl() != null) {
            fileStorageService.delete(entity.getImageUrl());
            entity.setImageUrl(null);
        }
        return mapper.toDto(entity);
    }

    OfferedService requireOfferedService(UUID businessId, UUID serviceId) {
        return repository.findByBusiness_IdAndId(businessId, serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Xizmat topilmadi: " + serviceId));
    }
}
