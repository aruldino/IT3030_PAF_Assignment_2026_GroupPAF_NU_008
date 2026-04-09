package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.BookingRequest;
import com.campus.smart_campus.exception.BusinessException;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.exception.UnauthorizedException;
import com.campus.smart_campus.model.Booking;
import com.campus.smart_campus.model.BookingStatus;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.AppUser;
import com.campus.smart_campus.repository.BookingRepository;
import com.campus.smart_campus.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceService resourceService;
    private final AppUserRepository appUserRepository;

    public BookingService(BookingRepository bookingRepository, ResourceService resourceService, AppUserRepository appUserRepository) {
        this.bookingRepository = bookingRepository;
        this.resourceService = resourceService;
        this.appUserRepository = appUserRepository;
    }

    public List<Booking> getBookings(BookingStatus status) {
        List<Booking> bookings = status == null
                ? bookingRepository.findAll()
                : bookingRepository.findByStatusOrderByBookingDateAscStartTimeAsc(status);

        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getStartTime))
                .toList();
    }

    public List<Booking> getBookingHistory() {
        return bookingRepository.findAll().stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .toList();
    }

    public Booking createBooking(BookingRequest request) {
        validateTimeRange(request);

        Resource resource = resourceService.getResourceById(request.resourceId());
        if (resource.getStatus() == ResourceStatus.OUT_OF_SERVICE) {
            throw new BusinessException("Bookings cannot be created for an out-of-service resource.");
        }

        if (request.expectedAttendees() > resource.getCapacity()) {
            throw new BusinessException("Expected attendees exceed the selected resource capacity.");
        }

        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setRequestedBy(request.requestedBy().trim());
        booking.setDepartment(request.department().trim());
        booking.setAcademicYear(request.academicYear().trim());
        booking.setSemester(request.semester().trim());
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setPurpose(request.purpose().trim());
        booking.setExpectedAttendees(request.expectedAttendees());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(@NonNull Long id, @NonNull BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        applyStatusTransition(booking, status);

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(@NonNull Long id) {
        return updateBookingStatus(id, BookingStatus.APPROVED);
    }

    public Booking rejectBooking(@NonNull Long id) {
        return updateBookingStatus(id, BookingStatus.REJECTED);
    }

    public Booking cancelBooking(@NonNull Long id) {
        return updateBookingStatus(id, BookingStatus.CANCELLED);
    }

    public List<Booking> getMyBookings(HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        String fullName = currentUser.getFullName().trim().toLowerCase();
        String email = currentUser.getEmail().trim().toLowerCase();
        return bookingRepository.findAll().stream()
                .filter(booking -> {
                    String requestedBy = booking.getRequestedBy().trim().toLowerCase();
                    return requestedBy.equals(fullName) || requestedBy.equals(email);
                })
                .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getStartTime))
                .toList();
    }

    public List<Booking> getConflicts(Long resourceId, java.time.LocalDate bookingDate, java.time.LocalTime startTime, java.time.LocalTime endTime) {
        return bookingRepository.findConflicts(resourceId, bookingDate, startTime, endTime);
    }

    public List<Booking> expirePendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        List<Booking> expired = new ArrayList<>();

        bookingRepository.findPendingCreatedBefore(cutoff).forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            expired.add(bookingRepository.save(booking));
        });

        return expired;
    }

    private void applyStatusTransition(Booking booking, BookingStatus targetStatus) {
        if (targetStatus == BookingStatus.APPROVED && bookingRepository.hasApprovedConflict(
                booking.getResource().getId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime())) {
            throw new BusinessException("This booking conflicts with an already approved booking for the same resource.");
        }

        if (targetStatus == BookingStatus.CANCELLED
                && booking.getStatus() == BookingStatus.PENDING) {
            throw new BusinessException("Pending bookings must be reviewed before cancellation.");
        }

        if (targetStatus == BookingStatus.APPROVED && booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Cancelled bookings cannot be approved again.");
        }
    }

    private AppUser getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(com.campus.smart_campus.service.AuthService.SESSION_USER_ID);
        if (userId == null) {
            throw new UnauthorizedException("Please log in to continue.");
        }
        return appUserRepository.findById((Long) userId)
                .orElseThrow(() -> new NotFoundException("Authenticated user was not found."));
    }

    private void validateTimeRange(BookingRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("End time must be later than start time.");
        }
    }
}
