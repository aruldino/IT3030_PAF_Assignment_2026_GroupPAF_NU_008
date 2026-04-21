package com.campus.smart_campus.modules.bookings.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.lang.NonNull;

public record BookingRequest(
        @NotNull(message = "Resource is required") @NonNull Long resourceId,
        @NotBlank(message = "Requester name is required") @NonNull String requestedBy,
        @NotBlank(message = "Department is required") @NonNull String department,
        @NotBlank(message = "Academic year is required") @NonNull String academicYear,
        @NotBlank(message = "Semester is required") @NonNull String semester,
        @NotNull(message = "Booking date is required") @FutureOrPresent(message = "Booking date must be today or later") @NonNull LocalDate bookingDate,
        @NotNull(message = "Start time is required") @NonNull LocalTime startTime,
        @NotNull(message = "End time is required") @NonNull LocalTime endTime,
        @NotBlank(message = "Purpose is required") @NonNull String purpose,
        @Min(value = 1, message = "Expected attendees must be at least 1") int expectedAttendees
) {
}

