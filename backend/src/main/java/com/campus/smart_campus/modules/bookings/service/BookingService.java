package com.campus.smart_campus.modules.bookings.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.common.error.NotFoundException;
import com.campus.smart_campus.common.error.UnauthorizedException;
import com.campus.smart_campus.modules.auth.service.RoleAccess;
import com.campus.smart_campus.modules.bookings.dto.BookingRequest;
import com.campus.smart_campus.modules.bookings.model.Booking;
import com.campus.smart_campus.modules.bookings.model.BookingStatus;
import com.campus.smart_campus.modules.bookings.repository.BookingRepository;
import com.campus.smart_campus.modules.resources.model.Resource;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;
import com.campus.smart_campus.modules.resources.service.ResourceService;
import com.campus.smart_campus.modules.users.model.AppUser;
import com.campus.smart_campus.modules.users.repository.AppUserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceService resourceService;
    private final AppUserRepository appUserRepository;

    public BookingService(BookingRepository bookingRepository, ResourceService resourceService,
            AppUserRepository appUserRepository) {
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

    public List<Booking> getBookings(BookingStatus status, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (isManagerOrStaff(currentUser)) {
            return getBookings(status);
        }

        if (RoleAccess.canCreateBookings(currentUser.getRole())) {
            return getMyBookings(session).stream()
                    .filter(booking -> status == null || booking.getStatus() == status)
                    .toList();
        }

        throw new UnauthorizedException("Technicians cannot access booking listings.");
    }

    public List<Booking> getBookingHistory(HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (isManagerOrStaff(currentUser)) {
            return getBookingHistory();
        }

        return getMyBookings(session);
    }

    public Booking createBooking(BookingRequest request, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canCreateBookings(currentUser.getRole())) {
            throw new UnauthorizedException("This role cannot create bookings.");
        }

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
        booking.setAcademicYear(cleanOptional(request.academicYear()));
        booking.setSemester(cleanOptional(request.semester()));
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setPurpose(request.purpose().trim());
        booking.setExpectedAttendees(request.expectedAttendees());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(@NonNull Long id, @NonNull BookingStatus status, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canManageBookings(currentUser.getRole())) {
            throw new UnauthorizedException("Only administrators can change booking status.");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        applyStatusTransition(booking, status);

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(@NonNull Long id, HttpSession session) {
        return updateBookingStatus(id, BookingStatus.APPROVED, session);
    }

    public Booking rejectBooking(@NonNull Long id, HttpSession session) {
        return updateBookingStatus(id, BookingStatus.REJECTED, session);
    }

    public Booking cancelBooking(@NonNull Long id, HttpSession session) {
        return updateBookingStatus(id, BookingStatus.CANCELLED, session);
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

    public List<Booking> getConflicts(Long resourceId, java.time.LocalDate bookingDate, java.time.LocalTime startTime,
            java.time.LocalTime endTime) {
        return bookingRepository.findConflicts(resourceId, bookingDate, startTime, endTime);
    }

    public List<Booking> expirePendingBookings(HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canManageBookings(currentUser.getRole())) {
            throw new UnauthorizedException("Only administrators can expire pending bookings.");
        }

        LocalDateTime cutoff = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        List<Booking> expired = new ArrayList<>();

        bookingRepository.findPendingCreatedBefore(cutoff).forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            expired.add(bookingRepository.save(booking));
        });

        return expired;
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
            throw new BusinessException(
                    "This booking conflicts with an already approved booking for the same resource.");
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
        Object userId = session.getAttribute(com.campus.smart_campus.modules.auth.service.AuthService.SESSION_USER_ID);
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

    private boolean isManagerOrStaff(AppUser user) {
        return RoleAccess.canManageBookings(user.getRole())
                || RoleAccess.normalize(user.getRole()) == com.campus.smart_campus.modules.users.model.UserRole.STAFF;
    }

    private String cleanOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
