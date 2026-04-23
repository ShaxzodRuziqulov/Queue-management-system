package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.dto.OfferedServiceCreateRequest;
import com.example.queuemanagementsystem.dto.OfferedServiceDto;
import com.example.queuemanagementsystem.dto.OfferedServiceUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OfferedServiceMapper {

    @Mapping(target = "businessId", source = "business.id")
    OfferedServiceDto toDto(OfferedService entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OfferedService toEntity(OfferedServiceCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget OfferedService entity, OfferedServiceUpdateRequest request);
}
