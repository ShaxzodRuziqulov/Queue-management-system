package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.UserIdentity;
import com.example.queuemanagementsystem.dto.UserIdentityCreateRequest;
import com.example.queuemanagementsystem.dto.UserIdentityDto;
import com.example.queuemanagementsystem.dto.UserIdentityUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.UserIdentityMapper;
import com.example.queuemanagementsystem.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserIdentityService {

    private final UserIdentityRepository repository;
    private final UserIdentityMapper mapper;
    private final AppUserService userService;

    @Transactional(readOnly = true)
    public List<UserIdentityDto> findAll(UUID userId) {
        userService.requireUser(userId);
        return repository.findByUser_Id(userId).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserIdentityDto get(UUID userId, UUID identityId) {
        return mapper.toDto(requireIdentity(userId, identityId));
    }

    public UserIdentityDto create(UUID userId, UserIdentityCreateRequest request) {
        UserIdentity entity = mapper.toEntity(request);
        entity.setUser(userService.requireUser(userId));
        return mapper.toDto(repository.save(entity));
    }

    public UserIdentityDto update(UUID userId, UUID identityId, UserIdentityUpdateRequest request) {
        UserIdentity entity = requireIdentity(userId, identityId);
        mapper.update(entity, request);
        return mapper.toDto(entity);
    }

    public void delete(UUID userId, UUID identityId) {
        UserIdentity entity = requireIdentity(userId, identityId);
        repository.delete(entity);
    }

    UserIdentity requireIdentity(UUID userId, UUID identityId) {
        return repository.findByUser_IdAndId(userId, identityId)
                .orElseThrow(() -> new ResourceNotFoundException("Identifikator topilmadi: " + identityId));
    }
}
