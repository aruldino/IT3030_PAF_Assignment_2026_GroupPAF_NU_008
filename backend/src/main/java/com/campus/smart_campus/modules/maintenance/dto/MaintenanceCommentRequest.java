package com.campus.smart_campus.modules.maintenance.dto;

import jakarta.validation.constraints.NotBlank;

public record MaintenanceCommentRequest(
        @NotBlank String comment,
        @NotBlank String author
) {
}

