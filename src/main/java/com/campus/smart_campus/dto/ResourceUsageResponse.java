package com.campus.smart_campus.dto;

public record ResourceUsageResponse(
        Long resourceId,
        String resourceName,
        long bookingCount
) {
}
