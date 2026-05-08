package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.dto.RoleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper extends EntityMapper<RoleDto, Role>{

}