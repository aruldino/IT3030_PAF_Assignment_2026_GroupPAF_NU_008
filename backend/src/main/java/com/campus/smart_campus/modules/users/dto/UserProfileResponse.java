package com.campus.smart_campus.modules.users.dto;

import com.campus.smart_campus.modules.users.model.UserRole;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {
}


