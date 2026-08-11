package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.domain.enums.BusinessCategory;
import com.example.queuemanagementsystem.dto.PublicBusinessSummaryDto;
import com.example.queuemanagementsystem.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/businesses")
@RequiredArgsConstructor
public class PublicBusinessController {

    private final BusinessService service;

    @GetMapping
    public ResponseEntity<Page<PublicBusinessSummaryDto>> list(
            @RequestParam(required = false) BusinessCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "q") String query,
            @PageableDefault(size = 20, sort = "rating", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findPublic(category, city, query, pageable));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> cities() {
        return ResponseEntity.ok(service.findPublicCities());
    }
}
