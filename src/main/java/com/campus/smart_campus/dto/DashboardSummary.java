package com.campus.smart_campus.dto;

public record DashboardSummary(
        long totalResources,
        long activeResources,
        long outOfServiceResources,
        long pendingBookings,
        long approvedBookings,
        long openMaintenanceTickets,
        long resolvedMaintenanceTickets
) {
}
