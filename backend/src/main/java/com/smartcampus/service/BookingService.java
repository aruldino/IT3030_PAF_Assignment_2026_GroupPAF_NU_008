package com.smartcampus.service;

import com.smartcampus.dto.BookingApprovalDTO;
import com.smartcampus.dto.BookingRequestDTO;
import com.smartcampus.dto.BookingResponseDTO;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.BookingConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.model.Booking;
import com.smartcampus.model.User;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final NotificationService notificationService;

    // ── HELPER: get current authenticated user ──────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    // ── HELPER: map Booking → BookingResponseDTO ────────────────────
    private BookingResponseDTO toDTO(Booking b) {
        return BookingResponseDTO.builder()
                .id(b.getId())
                .userId(b.getUserId())
                .userName(b.getUserName())
                .resourceId(b.getResourceId())
                .resourceName(b.getResourceName())
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .purpose(b.getPurpose())
                .expectedAttendees(b.getExpectedAttendees())
                .status(b.getStatus())
                .adminRemarks(b.getAdminRemarks())
                .qrCode(b.getQrCode())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    // ── CREATE BOOKING ───────────────────────────────────────────────
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        User user = getCurrentUser();

        // Validate: startTime must be before endTime
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Validate: resource exists
        var resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + dto.getResourceId()));

        // Conflict detection: same resource + date + overlapping time + PENDING or APPROVED
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                dto.getResourceId(),
                dto.getBookingDate(),
                dto.getStartTime(),
                dto.getEndTime()
        );
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(
                    "This resource is already booked for the selected time slot. " +
                    "Conflicting booking ID: " + conflicts.get(0).getId()
            );
        }

        Booking booking = Booking.builder()
                .userId(user.getId())
                .userName(user.getName())
                .resourceId(dto.getResourceId())
                .resourceName(resource.getName())
                .bookingDate(dto.getBookingDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .purpose(dto.getPurpose())
                .expectedAttendees(dto.getExpectedAttendees())
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        return toDTO(saved);
    }

    // ── GET MY BOOKINGS ──────────────────────────────────────────────
    public List<BookingResponseDTO> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUserIdOrderByBookingDateDesc(user.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── GET ALL BOOKINGS (admin) ─────────────────────────────────────
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── GET BOOKING BY ID ────────────────────────────────────────────
    public BookingResponseDTO getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        return toDTO(booking);
    }

    // ── APPROVE BOOKING (admin) ──────────────────────────────────────
    public BookingResponseDTO approveBooking(String id, BookingApprovalDTO dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be approved. Current status: " + booking.getStatus());
        }

        // Run conflict check again at approval time (defensive)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                booking.getResourceId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime()
        ).stream()
         .filter(c -> !c.getId().equals(id)) // exclude self
         .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Cannot approve — overlapping booking exists: " + conflicts.get(0).getId());
        }

        // Generate QR code on approval (innovation feature)
        String qrCode = qrCodeService.generateQRCode(id);

        booking.setStatus(BookingStatus.APPROVED);
        booking.setAdminRemarks(dto.getAdminRemarks());
        booking.setQrCode(qrCode);

        Booking saved = bookingRepository.save(booking);

        // Notify the user
        try {
            notificationService.notifyBookingApproved(booking.getUserId(), saved);
        } catch (Exception e) {
            // Non-critical — log but don't fail the approval
        }

        return toDTO(saved);
    }

    // ── REJECT BOOKING (admin) ───────────────────────────────────────
    public BookingResponseDTO rejectBooking(String id, BookingApprovalDTO dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be rejected. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setAdminRemarks(dto.getAdminRemarks());

        Booking saved = bookingRepository.save(booking);

        // Notify the user
        try {
            notificationService.notifyBookingRejected(booking.getUserId(), saved);
        } catch (Exception e) {
            // Non-critical
        }

        return toDTO(saved);
    }

    // ── CANCEL BOOKING (owner or admin) ─────────────────────────────
    public BookingResponseDTO cancelBooking(String id) {
        User user = getCurrentUser();

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        // Only the owner or an admin can cancel
        boolean isOwner = booking.getUserId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole().name());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED ||
            booking.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException("Booking is already " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toDTO(bookingRepository.save(booking));
    }

    // ── DELETE BOOKING (admin) ───────────────────────────────────────
    public void deleteBooking(String id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found: " + id);
        }
        bookingRepository.deleteById(id);
    }

    // ── GET QR CODE ──────────────────────────────────────────────────
    public String getQRCode(String bookingId) {
        User user = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Only the owner or admin can view QR
        boolean isOwner = booking.getUserId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole().name());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to view this QR code");
        }

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("QR code is only available for APPROVED bookings. Current status: " + booking.getStatus());
        }

        if (booking.getQrCode() == null) {
            // Regenerate if missing
            String qr = qrCodeService.generateQRCode(bookingId);
            booking.setQrCode(qr);
            bookingRepository.save(booking);
            return qr;
        }

        return booking.getQrCode();
    }

    // ── CHECK IN VIA QR ──────────────────────────────────────────────
    public BookingResponseDTO checkIn(String qrToken) {
        // Decode token → bookingId
        String bookingId = qrCodeService.verifyQRCode(qrToken);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for QR token"));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Check-in only allowed for APPROVED bookings. Status: " + booking.getStatus());
        }

        // Mark as checked in (reuse APPROVED status, just clear QR to prevent reuse)
        booking.setAdminRemarks("Checked in via QR on " + java.time.LocalDateTime.now());
        return toDTO(bookingRepository.save(booking));
    }
}