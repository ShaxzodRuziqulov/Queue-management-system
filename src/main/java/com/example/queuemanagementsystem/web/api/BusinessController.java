package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.BusinessCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessDto;
import com.example.queuemanagementsystem.dto.BusinessReviewRequest;
import com.example.queuemanagementsystem.dto.BusinessStatusRequest;
import com.example.queuemanagementsystem.dto.BusinessUpdateRequest;
import com.example.queuemanagementsystem.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService service;

    @GetMapping
    public ResponseEntity<Page<BusinessDto>> list(
            @RequestParam(required = false) UUID ownerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findAll(ownerId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BusinessDto> create(@Valid @RequestBody BusinessCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Admin only: biznes statusini o'zgartirish (ACTIVE, SUSPENDED, va h.k.) */
    @PutMapping("/{id}/status")
    public ResponseEntity<BusinessDto> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request));
    }

    /** Admin only: PENDING_REVIEW biznesni tasdiqlash yoki rad etish */
    @PostMapping("/{id}/review")
    public ResponseEntity<BusinessDto> review(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessReviewRequest request) {
        return ResponseEntity.ok(service.review(id, request));
    }
}
