package com.campus.smart_campus.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingSlotResponse(
        Long bookingId,
        LocalDate bookingDate,
        LocalTime startTime,
        LocalTime endTime,
        String status
) {
}
