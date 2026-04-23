package com.smartcampus.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for ticket file attachments — embedded in TicketResponseDTO.
 * Member 3 — Incident tickets.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAttachmentDTO {

    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadedAt;
}
