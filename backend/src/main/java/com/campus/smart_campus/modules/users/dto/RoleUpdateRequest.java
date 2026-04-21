package com.campus.smart_campus.modules.users.dto;

import com.campus.smart_campus.modules.users.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull Long userId,
        @NotNull UserRole role
) {
}


