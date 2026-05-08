package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.BookingDto;
import com.example.queuemanagementsystem.dto.StaffMemberDto;
import com.example.queuemanagementsystem.dto.StaffStatsDto;
import com.example.queuemanagementsystem.service.StaffMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Xodimlarning shaxsiy portali uchun endpointlar.
 * ROLE_STAFF roli bo'lgan foydalanuvchilar uchun.
 */
@RestController
@RequestMapping("/api/v1/staff/me")
@RequiredArgsConstructor
public class StaffPortalController {

    private final StaffMemberService service;

    /** Joriy xodimning profil ma'lumotlari */
    @GetMapping
    public ResponseEntity<StaffMemberDto> myProfile() {
        return ResponseEntity.ok(service.getMyProfile());
    }

    /** Joriy xodimga biriktirilgan bronlar */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> myBookings() {
        return ResponseEntity.ok(service.getMyBookings());
    }

    /** Joriy xodimning statistikasi */
    @GetMapping("/stats")
    public ResponseEntity<StaffStatsDto> myStats() {
        return ResponseEntity.ok(service.getMyStats());
    }
}
