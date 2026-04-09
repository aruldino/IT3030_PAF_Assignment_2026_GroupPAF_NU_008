package com.campus.smart_campus.dto;

import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

public record ResourceRequest(
        @NotBlank(message = "Name is required") @NonNull String name,
        @NotNull(message = "Type is required") @NonNull ResourceType type,
        @Min(value = 1, message = "Capacity must be at least 1") int capacity,
        @NotBlank(message = "Location is required") @NonNull String location,
        @NotBlank(message = "Availability window is required") @NonNull String availabilityWindow,
        @NotNull(message = "Status is required") @NonNull ResourceStatus status,
        @NotBlank(message = "Description is required") @NonNull String description
) {
}
