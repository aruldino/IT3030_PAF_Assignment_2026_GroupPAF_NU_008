package com.smartcampus.controller;

import com.smartcampus.dto.BookingApprovalDTO;
import com.smartcampus.dto.BookingRequestDTO;
import com.smartcampus.dto.BookingResponseDTO;
import com.smartcampus.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Booking REST controller — Member 2.
 * Auth is provided by Member 4's JWT filter.
 * No X-User-Id header needed — user extracted from SecurityContext.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** POST /api/bookings — Submit a new booking (any logged-in user) */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(dto));
    }

    /** GET /api/bookings/my — Get current user's own bookings */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    /** GET /api/bookings — Get all bookings (admin only) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /** GET /api/bookings/{id} — Get booking by ID */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /** PUT /api/bookings/{id}/approve — Approve a booking (admin only) */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> approveBooking(
            @PathVariable String id,
            @RequestBody(required = false) BookingApprovalDTO dto) {
        // dto is optional for approve (remarks not mandatory)
        if (dto == null) dto = new BookingApprovalDTO();
        return ResponseEntity.ok(bookingService.approveBooking(id, dto));
    }

    /** PUT /api/bookings/{id}/reject — Reject a booking (admin only) */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> rejectBooking(
            @PathVariable String id,
            @Valid @RequestBody BookingApprovalDTO dto) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, dto));
    }

    /** PATCH /api/bookings/{id}/cancel — Cancel a booking (owner or admin) */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    /** DELETE /api/bookings/{id} — Delete a booking record (admin only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/bookings/{id}/qr — Get QR code for an approved booking */
    @GetMapping("/{id}/qr")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getQRCode(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.getQRCode(id));
    }

    /** POST /api/bookings/check-in — QR code check-in */
    @PostMapping("/check-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> checkIn(@RequestParam String token) {
        return ResponseEntity.ok(bookingService.checkIn(token));
    }
}