package com.campus.smart_campus.dto;

public record NotificationPreferenceRequest(
        boolean resourceAlerts,
        boolean bookingAlerts,
        boolean maintenanceAlerts,
        boolean announcementAlerts
) {
}
