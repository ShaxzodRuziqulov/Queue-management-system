package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

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
}
