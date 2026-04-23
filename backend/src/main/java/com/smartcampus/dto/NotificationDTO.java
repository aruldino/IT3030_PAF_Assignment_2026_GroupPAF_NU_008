package com.smartcampus.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for notification responses.
 * Member 4 — Notification management.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private String id;
    private String title;
    private String message;
    private String type;
    private String referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
