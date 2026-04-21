package com.campus.smart_campus.modules.resources.dto;

import java.util.List;

import com.campus.smart_campus.modules.bookings.dto.BookingSlotResponse;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;

public record ResourceAvailabilityResponse(
                Long resourceId,
                String resourceName,
                String availabilityWindow,
                ResourceStatus status,
                List<BookingSlotResponse> bookedSlots) {
}
