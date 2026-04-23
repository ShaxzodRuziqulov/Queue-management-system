package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.dto.StaffMemberCreateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffMemberUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.StaffMemberMapper;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
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
    private final BusinessService businessService;
    private final AppUserService userService;

    @Transactional(readOnly = true)
    public List<StaffMemberDto> findAll(UUID businessId) {
        businessService.requireBusiness(businessId);
        return repository.findByBusiness_Id(businessId).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public StaffMemberDto get(UUID businessId, UUID staffId) {
        return mapper.toDto(requireStaff(businessId, staffId));
    }

    public StaffMemberDto create(UUID businessId, StaffMemberCreateRequest request) {
        StaffMember entity = mapper.toEntity(request);
        entity.setBusiness(businessService.requireBusiness(businessId));
        if (request.getLinkedUserId() != null) {
            entity.setLinkedUser(userService.requireUser(request.getLinkedUserId()));
        }
        return mapper.toDto(repository.save(entity));
    }

    public StaffMemberDto update(UUID businessId, UUID staffId, StaffMemberUpdateRequest request) {
        StaffMember entity = requireStaff(businessId, staffId);
        mapper.update(entity, request);
        if (request.getLinkedUserId() != null) {
            entity.setLinkedUser(userService.requireUser(request.getLinkedUserId()));
        }
        return mapper.toDto(entity);
    }

    public void delete(UUID businessId, UUID staffId) {
        StaffMember entity = requireStaff(businessId, staffId);
        repository.delete(entity);
    }

    StaffMember requireStaff(UUID businessId, UUID staffId) {
        return repository.findByBusiness_IdAndId(businessId, staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Xodim topilmadi: " + staffId));
    }
}
