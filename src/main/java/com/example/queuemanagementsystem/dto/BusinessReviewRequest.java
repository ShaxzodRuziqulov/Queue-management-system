package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.ReviewAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class BusinessReviewRequest {

    @NotNull(message = "Harakat (APPROVE yoki REJECT) majburiy")
    private ReviewAction action;

    /** Rad etish sababini yoki tasdiqlash izohini ko'rsating */
    private String note;

    /** Faqat APPROVE uchun: obuna tugash sanasi */
    private Instant subscriptionEndDate;
}
