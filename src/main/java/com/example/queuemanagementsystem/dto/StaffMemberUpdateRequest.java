package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class StaffMemberUpdateRequest {

    @Size(max = 200)
    private String displayName;

    private UUID linkedUserId;

    private Boolean active;
}
