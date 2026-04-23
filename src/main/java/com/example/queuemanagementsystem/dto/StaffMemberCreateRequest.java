package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class StaffMemberCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String displayName;

    private UUID linkedUserId;

    private boolean active = true;
}
