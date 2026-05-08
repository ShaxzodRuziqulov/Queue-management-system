package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.BusinessHours;
import com.example.queuemanagementsystem.dto.BusinessHoursCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessHoursDto;
import com.example.queuemanagementsystem.dto.BusinessHoursUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface BusinessHoursMapper extends EntityMapper<BusinessHoursDto, BusinessHours> {

    @Mapping(target = "businessId", source = "business.id")
    BusinessHoursDto toDto(BusinessHours entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    BusinessHours toEntity(BusinessHoursCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "weekday", ignore = true)
    void update(@MappingTarget BusinessHours entity, BusinessHoursUpdateRequest request);
}
