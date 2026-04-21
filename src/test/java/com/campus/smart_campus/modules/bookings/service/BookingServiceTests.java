package com.campus.smart_campus.modules.bookings.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.modules.auth.service.AuthService;
import com.campus.smart_campus.modules.bookings.dto.BookingRequest;
import com.campus.smart_campus.modules.bookings.model.Booking;
import com.campus.smart_campus.modules.bookings.repository.BookingRepository;
import com.campus.smart_campus.modules.resources.model.Resource;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;
import com.campus.smart_campus.modules.resources.service.ResourceService;
import com.campus.smart_campus.modules.users.model.AppUser;
import com.campus.smart_campus.modules.users.model.UserRole;
import com.campus.smart_campus.modules.users.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ResourceService resourceService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private HttpSession session;

    @Test
    void createBookingAllowsNullAcademicPeriodFields() {
        BookingService service = new BookingService(bookingRepository, resourceService, appUserRepository);

        AppUser user = new AppUser();
        user.setId(99L);
        user.setFullName("Test Student");
        user.setEmail("student@example.com");
        user.setRole(UserRole.STUDENT);

        Resource resource = new Resource();
        resource.setId(12L);
        resource.setCapacity(100);
        resource.setStatus(ResourceStatus.ACTIVE);

        BookingRequest request = new BookingRequest(
                12L,
                "Test Student",
                "Computing",
                null,
                null,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Lab practice",
                20
        );

        when(session.getAttribute(AuthService.SESSION_USER_ID)).thenReturn(99L);
        when(appUserRepository.findById(99L)).thenReturn(Optional.of(user));
        when(resourceService.getResourceById(12L)).thenReturn(resource);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.createBooking(request, session);

        assertNull(booking.getAcademicYear());
        assertNull(booking.getSemester());
    }

    @Test
    void createBookingRejectsInvalidTimeRange() {
        BookingService service = new BookingService(bookingRepository, resourceService, appUserRepository);

        AppUser user = new AppUser();
        user.setId(77L);
        user.setRole(UserRole.STUDENT);

        BookingRequest request = new BookingRequest(
                1L,
                "Test Student",
                "Computing",
                "Year 2",
                "Semester 1",
                LocalDate.now().plusDays(1),
                LocalTime.of(11, 0),
                LocalTime.of(10, 0),
                "Workshop",
                15
        );

        when(session.getAttribute(AuthService.SESSION_USER_ID)).thenReturn(77L);
        when(appUserRepository.findById(77L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> service.createBooking(request, session));
    }
}
