package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.CustomerCreateRequest;
import com.example.queuemanagementsystem.dto.CustomerDto;
import com.example.queuemanagementsystem.dto.CustomerUpdateRequest;
import com.example.queuemanagementsystem.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @GetMapping
    public ResponseEntity<Page<CustomerDto>> list(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(service.findAll(businessId, search, pageable));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto> get(@PathVariable UUID businessId, @PathVariable UUID customerId) {
        return ResponseEntity.ok(service.get(businessId, customerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CustomerDto> create(
            @PathVariable UUID businessId,
            @Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(businessId, request));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDto> update(
            @PathVariable UUID businessId,
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(service.update(businessId, customerId, request));
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID businessId, @PathVariable UUID customerId) {
        service.delete(businessId, customerId);
        return ResponseEntity.noContent().build();
    }
}
