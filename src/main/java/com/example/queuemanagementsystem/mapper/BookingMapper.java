package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.dto.BookingCreateRequest;
import com.example.queuemanagementsystem.dto.BookingDto;
import com.example.queuemanagementsystem.dto.BookingUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface BookingMapper extends EntityMapper<BookingDto, Booking> {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "offeredServiceId", source = "offeredService.id")
    @Mapping(target = "staffId", source = "staff.id")
    BookingDto toDto(Booking entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "offeredService", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "review", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking toEntity(BookingCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "offeredService", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "review", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Booking entity, BookingUpdateRequest request);
}
