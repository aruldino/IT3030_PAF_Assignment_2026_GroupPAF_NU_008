package com.campus.smart_campus.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record MaintenanceSlaResponse(
        Long ticketId,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        long responseMinutes,
        long resolutionMinutes,
        boolean withinResponseSla,
        boolean withinResolutionSla
) {
    public static MaintenanceSlaResponse of(
            Long ticketId,
            LocalDateTime createdAt,
            LocalDateTime resolvedAt,
            long responseMinutes,
            long resolutionMinutes,
            boolean withinResponseSla,
            boolean withinResolutionSla
    ) {
        return new MaintenanceSlaResponse(
                ticketId,
                createdAt,
                resolvedAt,
                responseMinutes,
                resolutionMinutes,
                withinResponseSla,
                withinResolutionSla
        );
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }
}
