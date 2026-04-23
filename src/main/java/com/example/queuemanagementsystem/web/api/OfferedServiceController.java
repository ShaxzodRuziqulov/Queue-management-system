package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.OfferedServiceCreateRequest;
import com.example.queuemanagementsystem.dto.OfferedServiceDto;
import com.example.queuemanagementsystem.dto.OfferedServiceUpdateRequest;
import com.example.queuemanagementsystem.service.OfferedServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/services")
@RequiredArgsConstructor
public class OfferedServiceController {

    private final OfferedServiceService service;

    @GetMapping
    public ResponseEntity<List<OfferedServiceDto>> list(@PathVariable UUID businessId) {
        return ResponseEntity.ok(service.findAll(businessId));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<OfferedServiceDto> get(@PathVariable UUID businessId, @PathVariable UUID serviceId) {
        return ResponseEntity.ok(service.get(businessId, serviceId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OfferedServiceDto> create(
            @PathVariable UUID businessId,
            @Valid @RequestBody OfferedServiceCreateRequest request) {
        return ResponseEntity.ok(service.create(businessId, request));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<OfferedServiceDto> update(
            @PathVariable UUID businessId,
            @PathVariable UUID serviceId,
            @Valid @RequestBody OfferedServiceUpdateRequest request) {
        return ResponseEntity.ok(service.update(businessId, serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID businessId, @PathVariable UUID serviceId) {
        service.delete(businessId, serviceId);
        return ResponseEntity.noContent().build();
    }
}
