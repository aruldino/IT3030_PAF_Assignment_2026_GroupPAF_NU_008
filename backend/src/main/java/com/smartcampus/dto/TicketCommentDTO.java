package com.smartcampus.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for ticket comment responses.
 * Member 3 — Incident ticket comments.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCommentDTO {

    private String id;
    private String userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
}
