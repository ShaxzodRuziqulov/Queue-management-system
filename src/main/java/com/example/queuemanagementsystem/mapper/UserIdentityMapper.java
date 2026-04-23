package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.UserIdentity;
import com.example.queuemanagementsystem.dto.UserIdentityCreateRequest;
import com.example.queuemanagementsystem.dto.UserIdentityDto;
import com.example.queuemanagementsystem.dto.UserIdentityUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserIdentityMapper {

    @Mapping(target = "userId", source = "user.id")
    UserIdentityDto toDto(UserIdentity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "linkedAt", ignore = true)
    UserIdentity toEntity(UserIdentityCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerSubject", ignore = true)
    @Mapping(target = "linkedAt", ignore = true)
    void update(@MappingTarget UserIdentity entity, UserIdentityUpdateRequest request);
}
