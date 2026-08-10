package com.example.queuemanagementsystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void sendPasswordResetCode(String email, String code) {

        log.info("Password reset code for {}: {}", email, code);
    }
}
