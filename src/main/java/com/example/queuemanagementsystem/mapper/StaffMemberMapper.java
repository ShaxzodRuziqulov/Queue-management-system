package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.dto.StaffMemberCreateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffMemberUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StaffMemberMapper extends EntityMapper<StaffMemberDto, StaffMember> {

    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "linkedUserId", source = "linkedUser.id")
    StaffMemberDto toDto(StaffMember entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "linkedUser", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StaffMember toEntity(StaffMemberCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "linkedUser", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget StaffMember entity, StaffMemberUpdateRequest request);
}
