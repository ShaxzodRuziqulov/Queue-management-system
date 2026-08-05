package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserLookupDto {
    private UUID id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
