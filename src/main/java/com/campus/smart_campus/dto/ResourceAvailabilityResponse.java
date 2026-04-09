package com.campus.smart_campus.dto;

import com.campus.smart_campus.model.ResourceStatus;
import java.util.List;

public record ResourceAvailabilityResponse(
        Long resourceId,
        String resourceName,
        String availabilityWindow,
        ResourceStatus status,
        List<BookingSlotResponse> bookedSlots
) {
}
