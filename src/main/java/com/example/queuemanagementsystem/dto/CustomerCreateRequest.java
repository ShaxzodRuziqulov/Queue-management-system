package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerCreateRequest {

    @NotBlank
    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    @Size(max = 120)
    private String middleName;

    @Size(max = 32)
    private String phone;

    @Email
    @Size(max = 320)
    private String email;

    private String note;
}
