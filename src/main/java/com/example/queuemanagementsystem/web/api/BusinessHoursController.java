package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.BusinessHoursCreateRequest;
import com.example.queuemanagementsystem.dto.BusinessHoursDto;
import com.example.queuemanagementsystem.dto.BusinessHoursUpdateRequest;
import com.example.queuemanagementsystem.service.BusinessHoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/hours")
@RequiredArgsConstructor
public class BusinessHoursController {

    private final BusinessHoursService service;

    @GetMapping
    public ResponseEntity<List<BusinessHoursDto>> list(@PathVariable UUID businessId) {
        return ResponseEntity.ok(service.findAll(businessId));
    }

    @GetMapping("/{hoursId}")
    public ResponseEntity<BusinessHoursDto> get(@PathVariable UUID businessId, @PathVariable UUID hoursId) {
        return ResponseEntity.ok(service.get(businessId, hoursId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BusinessHoursDto> create(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessHoursCreateRequest request) {
        return ResponseEntity.ok(service.create(businessId, request));
    }

    @PutMapping("/{hoursId}")
    public ResponseEntity<BusinessHoursDto> update(
            @PathVariable UUID businessId,
            @PathVariable UUID hoursId,
            @Valid @RequestBody BusinessHoursUpdateRequest request) {
        return ResponseEntity.ok(service.update(businessId, hoursId, request));
    }

    @DeleteMapping("/{hoursId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID businessId, @PathVariable UUID hoursId) {
        service.delete(businessId, hoursId);
        return ResponseEntity.noContent().build();
    }
}
