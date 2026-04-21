package com.campus.smart_campus.modules.notifications.dto;

public record NotificationPreferenceRequest(
        boolean resourceAlerts,
        boolean bookingAlerts,
        boolean maintenanceAlerts,
        boolean announcementAlerts
) {
}

