package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffStatsDto {
    private long totalBookings;
    private long completedBookings;
    private long pendingBookings;
    private long cancelledBookings;
    private double avgRating;
    private long reviewCount;
}
