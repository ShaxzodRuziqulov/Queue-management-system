package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.Review;
import com.example.queuemanagementsystem.dto.ReviewCreateRequest;
import com.example.queuemanagementsystem.dto.ReviewDto;
import com.example.queuemanagementsystem.dto.ReviewUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper extends EntityMapper<ReviewDto, Review> {

    @Mapping(target = "bookingId",  source = "booking.id")
    @Mapping(target = "businessId", source = "booking.business.id")
    @Mapping(target = "staffId",    source = "staff.id")
    @Mapping(target = "staffFirstName",  source = "staff.firstName")
    @Mapping(target = "staffLastName",  source = "staff.lastName")
    @Mapping(target = "customerFirstName", source = "booking.customer.firstName")
    @Mapping(target = "customerLastName", source = "booking.customer.lastName")
    ReviewDto toDto(Review entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "booking",   ignore = true)
    @Mapping(target = "staff",     ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(ReviewCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "booking",   ignore = true)
    @Mapping(target = "staff",     ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Review entity, ReviewUpdateRequest request);
}
