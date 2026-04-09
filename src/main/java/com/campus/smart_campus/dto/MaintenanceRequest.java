package com.campus.smart_campus.dto;

import com.campus.smart_campus.model.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

public record MaintenanceRequest(
        @NotNull(message = "Resource is required") @NonNull Long resourceId,
        @NotBlank(message = "Issue type is required") @NonNull String issueType,
        @NotBlank(message = "Description is required") @NonNull String description,
        @NotBlank(message = "Reporter name is required") @NonNull String reportedBy,
        @NotNull(message = "Priority is required") @NonNull MaintenancePriority priority
) {
}
