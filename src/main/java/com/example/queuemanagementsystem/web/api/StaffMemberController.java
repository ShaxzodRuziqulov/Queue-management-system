package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.StaffAccountUpdateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberCreateRequest;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffMemberUpdateRequest;
import com.example.queuemanagementsystem.dto.StaffRegisterRequest;
import com.example.queuemanagementsystem.service.StaffMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/staff")
@RequiredArgsConstructor
public class StaffMemberController {

    private final StaffMemberService service;

    @GetMapping
    public ResponseEntity<List<StaffMemberDto>> list(@PathVariable UUID businessId) {
        return ResponseEntity.ok(service.findAll(businessId));
    }

    @GetMapping("/{staffId}")
    public ResponseEntity<StaffMemberDto> get(@PathVariable UUID businessId, @PathVariable UUID staffId) {
        return ResponseEntity.ok(service.get(businessId, staffId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StaffMemberDto> create(
            @PathVariable UUID businessId,
            @Valid @RequestBody StaffMemberCreateRequest request) {
        return ResponseEntity.ok(service.create(businessId, request));
    }

    /** Xodim uchun yangi foydalanuvchi hisobi (login/parol) yaratib, shu bilan birga bog'laydi. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StaffMemberDto> register(
            @PathVariable UUID businessId,
            @Valid @RequestBody StaffRegisterRequest request) {
        return ResponseEntity.ok(service.registerStaff(businessId, request));
    }

    /** Avval hisobsiz yaratilgan xodimga yangi foydalanuvchi hisobi (login/parol) ochib beradi. */
    @PostMapping("/{staffId}/register")
    public ResponseEntity<StaffMemberDto> registerAccountForExisting(
            @PathVariable UUID businessId,
            @PathVariable UUID staffId,
            @Valid @RequestBody StaffRegisterRequest request) {
        return ResponseEntity.ok(service.registerAccountForExisting(businessId, staffId, request));
    }

    /** Bog'langan hisobning ism/email/telefon/parolini yangilaydi. */
    @PutMapping("/{staffId}/account")
    public ResponseEntity<StaffMemberDto> updateAccount(
            @PathVariable UUID businessId,
            @PathVariable UUID staffId,
            @Valid @RequestBody StaffAccountUpdateRequest request) {
        return ResponseEntity.ok(service.updateLinkedAccount(businessId, staffId, request));
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<StaffMemberDto> update(
            @PathVariable UUID businessId,
            @PathVariable UUID staffId,
            @Valid @RequestBody StaffMemberUpdateRequest request) {
        return ResponseEntity.ok(service.update(businessId, staffId, request));
    }

    @DeleteMapping("/{staffId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID businessId, @PathVariable UUID staffId) {
        service.delete(businessId, staffId);
        return ResponseEntity.noContent().build();
    }
}
