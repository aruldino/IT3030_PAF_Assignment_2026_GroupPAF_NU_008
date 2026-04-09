package com.campus.smart_campus.dto;

import com.campus.smart_campus.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull Long userId,
        @NotNull UserRole role
) {
}
