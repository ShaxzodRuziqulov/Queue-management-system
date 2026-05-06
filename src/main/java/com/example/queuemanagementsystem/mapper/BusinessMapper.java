package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.dto.BusinessCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessDto;
import com.example.queuemanagementsystem.dto.BusinessUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "accessAllowed", expression = "java(entity.isAccessAllowed())")
    BusinessDto toDto(Business entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "trialEndDate", ignore = true)
    @Mapping(target = "subscriptionEndDate", ignore = true)
    @Mapping(target = "hours", ignore = true)
    @Mapping(target = "offeredServices", ignore = true)
    @Mapping(target = "staffMembers", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Business toEntity(BusinessCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "trialEndDate", ignore = true)
    @Mapping(target = "subscriptionEndDate", ignore = true)
    @Mapping(target = "hours", ignore = true)
    @Mapping(target = "offeredServices", ignore = true)
    @Mapping(target = "staffMembers", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Business entity, BusinessUpdateRequest request);
}
