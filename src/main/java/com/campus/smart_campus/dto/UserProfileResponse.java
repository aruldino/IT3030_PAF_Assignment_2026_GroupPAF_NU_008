package com.campus.smart_campus.dto;

import com.campus.smart_campus.model.UserRole;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {
}
