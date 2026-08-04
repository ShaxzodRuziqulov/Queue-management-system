package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.dto.StaffMemberCreateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffMemberUpdateRequest;
import org.mapstruct.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StaffMemberMapper extends EntityMapper<StaffMemberDto, StaffMember> {

    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "linkedUserId", source = "linkedUser.id")
    @Mapping(target = "serviceIds", expression = "java(toServiceIds(entity))")
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    StaffMemberDto toDto(StaffMember entity);

    default Set<UUID> toServiceIds(StaffMember entity) {
        if (entity.getOfferedServices() == null) {
            return Set.of();
        }
        return entity.getOfferedServices().stream()
                .map(service -> service.getId())
                .collect(Collectors.toSet());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "linkedUser", ignore = true)
    @Mapping(target = "offeredServices", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StaffMember toEntity(StaffMemberCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "linkedUser", ignore = true)
    @Mapping(target = "offeredServices", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget StaffMember entity, StaffMemberUpdateRequest request);
}
