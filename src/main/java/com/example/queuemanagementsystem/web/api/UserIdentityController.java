package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.UserIdentityCreateRequest;
import com.example.queuemanagementsystem.dto.UserIdentityDto;
import com.example.queuemanagementsystem.dto.UserIdentityUpdateRequest;
import com.example.queuemanagementsystem.service.UserIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/identities")
@RequiredArgsConstructor
public class UserIdentityController {

    private final UserIdentityService service;

    @GetMapping
    public ResponseEntity<List<UserIdentityDto>> list(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.findAll(userId));
    }

    @GetMapping("/{identityId}")
    public ResponseEntity<UserIdentityDto> get(@PathVariable UUID userId, @PathVariable UUID identityId) {
        return ResponseEntity.ok(service.get(userId, identityId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserIdentityDto> create(
            @PathVariable UUID userId,
            @Valid @RequestBody UserIdentityCreateRequest request) {
        return ResponseEntity.ok(service.create(userId, request));
    }

    @PutMapping("/{identityId}")
    public ResponseEntity<UserIdentityDto> update(
            @PathVariable UUID userId,
            @PathVariable UUID identityId,
            @Valid @RequestBody UserIdentityUpdateRequest request) {
        return ResponseEntity.ok(service.update(userId, identityId, request));
    }

    @DeleteMapping("/{identityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID identityId) {
        service.delete(userId, identityId);
        return ResponseEntity.noContent().build();
    }
}
