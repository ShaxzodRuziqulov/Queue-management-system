package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class StaffAccountUpdateRequest {
    @Size(max = 200)
    private String displayName;

    @Size(max = 320)

    private String email;

    @Size(max = 32)
    private String phone;

    @Size(min = 4, max = 128)
    private String password;
    private Set<UUID> serviceIds = new HashSet<>();
}
