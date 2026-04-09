package com.campus.smart_campus.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    public BookingExpiryScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000L)
    public void expirePendingBookings() {
        bookingService.expirePendingBookings();
    }
}
