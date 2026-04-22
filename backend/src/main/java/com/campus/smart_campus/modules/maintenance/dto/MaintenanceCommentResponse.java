package com.campus.smart_campus.modules.maintenance.dto;

import java.time.LocalDateTime;

public record MaintenanceCommentResponse(
        Long id,
        String author,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

