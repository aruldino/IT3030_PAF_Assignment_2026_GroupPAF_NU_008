package com.campus.smart_campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;

public record AnnouncementRequest(
        @NotBlank(message = "Title is required") @Size(max = 160, message = "Title must be 160 characters or fewer") @NonNull String title,
        @NotBlank(message = "Content is required") @Size(max = 1200, message = "Content must be 1200 characters or fewer") @NonNull String content
) {
}
