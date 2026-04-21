package com.campus.smart_campus.modules.maintenance.dto;

import jakarta.validation.constraints.NotBlank;

public record MaintenanceCommentEditRequest(
        @NotBlank String comment
) {
}

