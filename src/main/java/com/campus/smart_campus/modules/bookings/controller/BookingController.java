package com.campus.smart_campus.modules.bookings.controller;

import com.campus.smart_campus.modules.bookings.dto.BookingRequest;
import com.campus.smart_campus.modules.bookings.model.Booking;
import com.campus.smart_campus.modules.bookings.model.BookingStatus;
import com.campus.smart_campus.modules.bookings.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/bookings", "/bookings"})
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getBookings(@RequestParam(required = false) BookingStatus status, HttpSession session) {
        return bookingService.getBookings(status, session);
    }

    @GetMapping("/history")
    public List<Booking> getHistory(HttpSession session) {
        return bookingService.getBookingHistory(session);
    }

    @GetMapping("/my")
    public List<Booking> getMyBookings(HttpSession session) {
        return bookingService.getMyBookings(session);
    }

    @GetMapping("/conflicts")
    public List<Booking> getConflicts(
            @RequestParam Long resourceId,
            @RequestParam LocalDate bookingDate,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime
    ) {
        return bookingService.getConflicts(resourceId, bookingDate, startTime, endTime);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@Valid @RequestBody BookingRequest request, HttpSession session) {
        return bookingService.createBooking(request, session);
    }

    @PutMapping("/{id}/approve")
    public Booking approveBooking(@PathVariable Long id, HttpSession session) {
        return bookingService.approveBooking(id, session);
    }

    @PutMapping("/{id}/reject")
    public Booking rejectBooking(@PathVariable Long id, HttpSession session) {
        return bookingService.rejectBooking(id, session);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id, HttpSession session) {
        return bookingService.cancelBooking(id, session);
    }

    @PutMapping("/expire-pending")
    public List<Booking> expirePendingBookings(HttpSession session) {
        return bookingService.expirePendingBookings(session);
    }

    @PatchMapping("/{id}/status")
    public Booking updateStatus(@PathVariable Long id, @RequestParam BookingStatus status, HttpSession session) {
        return bookingService.updateBookingStatus(id, status, session);
    }
}


