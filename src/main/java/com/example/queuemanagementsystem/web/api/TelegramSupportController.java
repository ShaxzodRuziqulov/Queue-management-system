package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.TelegramLinkDto;
import com.example.queuemanagementsystem.service.TelegramSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
public class TelegramSupportController {
    private final TelegramSupportService service;

    @PostMapping("/link")
    public ResponseEntity<TelegramLinkDto> createLink(@RequestParam(defaultValue = "GENERAL") String source) {
        return ResponseEntity.ok(service.createLink(source));
    }
}
