package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.SupportReplyRequest;
import com.example.queuemanagementsystem.dto.SupportTicketDto;
import com.example.queuemanagementsystem.dto.SupportTicketUpdateRequest;
import com.example.queuemanagementsystem.service.SupportTicketService;
import com.example.queuemanagementsystem.service.TelegramSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SupportTicketController {
    private final SupportTicketService service;
    private final TelegramSupportService telegram;

    @GetMapping("/api/v1/support/tickets")
    public ResponseEntity<Page<SupportTicketDto>> mine(@PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable p) {
        return ResponseEntity.ok(service.mine(p));
    }

    @GetMapping("/api/v1/support/tickets/{id}")
    public ResponseEntity<SupportTicketDto> mineGet(@PathVariable UUID id) {
        return ResponseEntity.ok(service.mineGet(id));
    }

    @GetMapping("/api/v1/admin/support-tickets")
    public ResponseEntity<Page<SupportTicketDto>> list(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @PageableDefault(size = 30, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable p) {
        return ResponseEntity.ok(service.adminList(status, q, p));
    }

    @GetMapping("/api/v1/admin/support-tickets/{id}")
    public ResponseEntity<SupportTicketDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.adminGet(id));
    }

    @PatchMapping("/api/v1/admin/support-tickets/{id}")
    public ResponseEntity<SupportTicketDto> update(@PathVariable UUID id, @RequestBody SupportTicketUpdateRequest r) {
        return ResponseEntity.ok(service.adminGet(service.update(id, r).getId()));
    }

    @PostMapping("/api/v1/admin/support-tickets/{id}/messages")
    public ResponseEntity<SupportTicketDto> reply(@PathVariable UUID id, @Valid @RequestBody SupportReplyRequest r) {
        telegram.sendOperatorReply(service.addOperatorReply(id, r.content()), r.content());
        return ResponseEntity.ok(service.adminGet(id));
    }
}
