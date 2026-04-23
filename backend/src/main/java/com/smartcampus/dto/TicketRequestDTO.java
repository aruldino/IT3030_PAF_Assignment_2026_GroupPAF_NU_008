package com.smartcampus.dto;

import com.smartcampus.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for submitting a new incident ticket.
 * Member 3 — Incident tickets.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestDTO {

    /** Optional — links the ticket to a specific campus resource. */
    private String resourceId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    private String location;

    private String contactDetails;
}
