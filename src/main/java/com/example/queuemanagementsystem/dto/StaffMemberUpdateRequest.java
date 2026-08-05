package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class StaffMemberUpdateRequest {

    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    @Size(max = 1024)
    private String avatarUrl;

    private String bio;

    private Integer experienceYears;

    private UUID linkedUserId;

    private Set<UUID> serviceIds;

    private Boolean active;
}
