package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffAccountUpdateRequest {
    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    @Size(max = 320)
    private String email;

    @Size(max = 32)
    private String phone;

    @Size(min = 4, max = 128)
    private String password;
}
