package com.smartcampus.dto;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO returned in resource API responses.
 * Member 1 — Facilities catalogue.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponseDTO {

    private String id;
    private String name;
    private ResourceType type;
    private Integer capacity;
    private String location;
    private String description;
    private LocalTime availabilityStart;
    private LocalTime availabilityEnd;
    private ResourceStatus status;
    private LocalDateTime createdAt;
}
