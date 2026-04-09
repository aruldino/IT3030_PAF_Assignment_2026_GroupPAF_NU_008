package com.campus.smart_campus.dto;

import jakarta.validation.constraints.NotBlank;

public record MaintenanceCommentEditRequest(
        @NotBlank String comment
) {
}
