package com.campus.smart_campus.modules.notifications.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String category,
        boolean read,
        LocalDateTime createdAt
) {
}

