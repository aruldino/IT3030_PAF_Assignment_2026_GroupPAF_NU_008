package com.campus.smart_campus.modules.resources.dto;

public record ResourceUsageResponse(
        Long resourceId,
        String resourceName,
        long bookingCount
) {
}

