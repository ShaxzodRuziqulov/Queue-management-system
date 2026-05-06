package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.BusinessHours;
import com.example.queuemanagementsystem.dto.BusinessHoursCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessHoursDto;
import com.example.queuemanagementsystem.dto.BusinessHoursUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.BusinessHoursMapper;
import com.example.queuemanagementsystem.repository.BusinessHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessHoursService {

    private final BusinessHoursRepository repository;
    private final BusinessHoursMapper mapper;
    private final BusinessService businessService;

    @Transactional(readOnly = true)
    public List<BusinessHoursDto> findAll(UUID businessId) {
        businessService.requireBusiness(businessId);
        return repository.findByBusiness_IdOrderByWeekdayAsc(businessId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessHoursDto get(UUID businessId, UUID hoursId) {
        return mapper.toDto(requireHours(businessId, hoursId));
    }

    public BusinessHoursDto create(UUID businessId, BusinessHoursCreateRequest request) {
        BusinessHours entity = mapper.toEntity(request);
        entity.setBusiness(businessService.requireBusiness(businessId));
        return mapper.toDto(repository.save(entity));
    }

    public BusinessHoursDto update(UUID businessId, UUID hoursId, BusinessHoursUpdateRequest request) {
        businessService.requireOwnerOrAdmin(businessId);
        BusinessHours entity = requireHours(businessId, hoursId);
        mapper.update(entity, request);
        return mapper.toDto(entity);
    }

    public void delete(UUID businessId, UUID hoursId) {
        businessService.requireOwnerOrAdmin(businessId);
        BusinessHours entity = requireHours(businessId, hoursId);
        repository.delete(entity);
    }

    BusinessHours requireHours(UUID businessId, UUID hoursId) {
        return repository.findByBusiness_IdAndId(businessId, hoursId)
                .orElseThrow(() -> new ResourceNotFoundException("Ish vaqti yozuvi topilmadi: " + hoursId));
    }
}
