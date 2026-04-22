package com.campus.smart_campus.modules.maintenance.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MaintenanceAttachmentRequest(
        @NotEmpty List<String> attachments
) {
}

