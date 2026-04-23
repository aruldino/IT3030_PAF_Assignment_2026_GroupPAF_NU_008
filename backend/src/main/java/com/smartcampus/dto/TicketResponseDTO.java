package com.smartcampus.dto;

import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO returned in ticket API responses.
 * Member 3 — Incident tickets.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDTO {

    private String id;
    private String userId;
    private String userName;
    private String resourceId;
    private String resourceName;
    private String category;
    private String description;
    private TicketPriority priority;
    private String location;
    private String contactDetails;
    private TicketStatus status;
    private String assignedToId;
    private String assignedToName;
    private String resolutionNotes;
    private String rejectionReason;
    private List<TicketAttachmentDTO> attachments;
    private LocalDateTime createdAt;
}
