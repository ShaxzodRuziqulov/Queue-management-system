package com.example.queuemanagementsystem.domain.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ROLE_ADMIN("Admin"),
    ROLE_MANAGER("Manager"),
    ROLE_BUSINESS_OWNER("Business Owner"),
    ROLE_STAFF("Staff Member"),
    ROLE_USER("User");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

}
