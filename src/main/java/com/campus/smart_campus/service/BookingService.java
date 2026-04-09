package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.BookingRequest;
import com.campus.smart_campus.exception.BusinessException;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.model.Booking;
import com.campus.smart_campus.model.BookingStatus;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.repository.BookingRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceService resourceService;

    public BookingService(BookingRepository bookingRepository, ResourceService resourceService) {
        this.bookingRepository = bookingRepository;
        this.resourceService = resourceService;
    }

    public List<Booking> getBookings(BookingStatus status) {
        List<Booking> bookings = status == null
                ? bookingRepository.findAll()
                : bookingRepository.findByStatusOrderByBookingDateAscStartTimeAsc(status);

        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getStartTime))
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

        if (status == BookingStatus.APPROVED && bookingRepository.hasApprovedConflict(
                booking.getResource().getId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime())) {
            throw new BusinessException("This booking conflicts with an already approved booking for the same resource.");
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private void validateTimeRange(BookingRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("End time must be later than start time.");
        }
    }
}
