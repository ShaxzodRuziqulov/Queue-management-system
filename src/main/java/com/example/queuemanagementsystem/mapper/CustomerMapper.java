package com.example.queuemanagementsystem.mapper;

import com.example.queuemanagementsystem.domain.Customer;
import com.example.queuemanagementsystem.dto.CustomerCreateRequest;
import com.example.queuemanagementsystem.dto.CustomerDto;
import com.example.queuemanagementsystem.dto.CustomerUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper extends EntityMapper<CustomerDto, Customer> {

    @Mapping(target = "businessId", source = "business.id")
    CustomerDto toDto(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "visitCount", ignore = true)
    @Mapping(target = "lastVisitAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "visitCount", ignore = true)
    @Mapping(target = "lastVisitAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Customer entity, CustomerUpdateRequest request);
}
