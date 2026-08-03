package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateRequest {

    @Size(max = 200)
    private String fullName;

    @Size(max = 32)
    private String phone;

    @Email
    @Size(max = 320)
    private String email;

    private String note;

    private Boolean active;
}
