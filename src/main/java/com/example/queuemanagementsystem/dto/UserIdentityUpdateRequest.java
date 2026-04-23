package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserIdentityUpdateRequest {

    @Size(max = 320)
    private String providerEmail;
}
