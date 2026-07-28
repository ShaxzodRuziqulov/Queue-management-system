package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.AppUserCreateRequest;
import com.example.queuemanagementsystem.dto.ChangePasswordRequest;
import com.example.queuemanagementsystem.dto.AppUserDto;
import com.example.queuemanagementsystem.dto.AppUserUpdateRequest;
import com.example.queuemanagementsystem.dto.UserLookupDto;
import com.example.queuemanagementsystem.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService service;

    @GetMapping
    public ResponseEntity<List<AppUserDto>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    /** Xodimni mavjud foydalanuvchi hisobiga bog'lash uchun login bo'yicha qidirish (minimal ma'lumot). */
    @GetMapping("/by-login/{login}")
    public ResponseEntity<UserLookupDto> byLogin(@PathVariable String login) {
        return ResponseEntity.ok(service.findByLogin(login));
    }

    /**
     * Telefon raqami bo'yicha ro'yxatdagi mijozni qidirish — xodim mijozni qo'lda bron
     * qilganda ishlatiladi. Topilsa mijozni bog'lash uchun, topilmasa mehmon sifatida kiritiladi.
     */
    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<List<UserLookupDto>> byPhone(@PathVariable String phone) {
        return ResponseEntity.ok(service.findByPhone(phone));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AppUserDto> create(@Valid @RequestBody AppUserCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserDto> update(@PathVariable UUID id, @Valid @RequestBody AppUserUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /** Joriy (tizimga kirgan) foydalanuvchi o'z parolini o'zgartiradi. */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppUserDto> uploadAvatar(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadAvatar(id, file));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
