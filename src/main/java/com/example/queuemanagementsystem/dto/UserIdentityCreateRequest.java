package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.AuthProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserIdentityCreateRequest {

    @NotNull
    private AuthProvider provider;

    @NotNull
    @Size(max = 256)
    private String providerSubject;

    @Size(max = 320)
    private String providerEmail;
}
