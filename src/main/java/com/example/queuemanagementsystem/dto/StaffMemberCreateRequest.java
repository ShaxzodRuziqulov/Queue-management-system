package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class StaffMemberCreateRequest {

    @NotBlank
    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    @Size(max = 1024)
    private String avatarUrl;

    private String bio;

    private Integer experienceYears;

    private UUID linkedUserId;

    private boolean active = true;

    private Set<UUID> serviceIds = new HashSet<>();

}
