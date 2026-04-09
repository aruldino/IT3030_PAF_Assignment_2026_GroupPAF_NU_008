package com.campus.smart_campus.config;

import com.campus.smart_campus.model.Booking;
import com.campus.smart_campus.model.BookingStatus;
import com.campus.smart_campus.model.MaintenancePriority;
import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.MaintenanceTicket;
import com.campus.smart_campus.model.Announcement;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.ResourceType;
import com.campus.smart_campus.model.UserRole;
import com.campus.smart_campus.repository.AnnouncementRepository;
import com.campus.smart_campus.repository.BookingRepository;
import com.campus.smart_campus.repository.MaintenanceTicketRepository;
import com.campus.smart_campus.repository.ResourceRepository;
import com.campus.smart_campus.repository.AppUserRepository;
import com.campus.smart_campus.service.AuthService;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedDemoData(
            AnnouncementRepository announcementRepository,
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            MaintenanceTicketRepository maintenanceTicketRepository,
            AppUserRepository appUserRepository,
            JdbcTemplate jdbcTemplate,
            AuthService authService
    ) {
        return args -> {
            widenUserRoleColumn(jdbcTemplate);
            widenResourceStatusColumn(jdbcTemplate);
            authService.createSeedUser("Campus Super Administrator", "superadmin@smartcampus.lk", "SuperAdmin@123", UserRole.SUPER_ADMIN);
            authService.createSeedUser("Campus Administrator", "admin@smartcampus.lk", "Admin@123", UserRole.ADMIN);
            authService.createSeedUser("Campus Student", "student@smartcampus.lk", "Student@123", UserRole.STUDENT);
            authService.createSeedUser("Campus Staff", "staff@smartcampus.lk", "Staff@123", UserRole.STAFF);
            authService.createSeedUser("Campus User", "user@smartcampus.lk", "User@123", UserRole.USER);
            authService.createSeedUser("Campus Technician", "tech@smartcampus.lk", "Tech@123", UserRole.TECHNICIAN);

            if (resourceRepository.count() == 0) {
                Resource aiLab = createResource(
                        "AI Innovation Lab",
                        ResourceType.LAB,
                        40,
                        "Engineering Building - Level 4",
                        "Mon-Fri 08:00-18:00",
                        ResourceStatus.ACTIVE,
                        "High-performance computing lab for ML, data science, and capstone sessions."
                );
                Resource auditorium = createResource(
                        "Main Auditorium",
                        ResourceType.LECTURE_HALL,
                        220,
                        "Administration Block",
                        "Mon-Sat 08:00-20:00",
                        ResourceStatus.ACTIVE,
                        "Large event space for guest talks, assessments, and orientation programs."
                );
                Resource meetingRoom = createResource(
                        "Strategy Room B",
                        ResourceType.MEETING_ROOM,
                        18,
                        "Business School - Level 2",
                        "Mon-Fri 09:00-17:00",
                        ResourceStatus.ACTIVE,
                        "Hybrid meeting room with conference display and collaboration tools."
                );
                Resource projectorKit = createResource(
                        "Projector Kit 07",
                        ResourceType.EQUIPMENT,
                        1,
                        "Media Store",
                        "Daily 08:30-16:30",
                        ResourceStatus.MAINTENANCE,
                        "Portable projector bundle currently awaiting lamp replacement."
                );

                @SuppressWarnings({"null", "unused"})
                var savedResources = resourceRepository.saveAll(List.of(aiLab, auditorium, meetingRoom, projectorKit));

                @SuppressWarnings({"null", "unused"})
                var booking1 = bookingRepository.save(createBooking(
                        auditorium,
                        "Nethmi Perera",
                        "Student Affairs",
                        LocalDate.now().plusDays(2),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        "Orientation briefing",
                        180,
                        BookingStatus.APPROVED
                ));
                @SuppressWarnings({"null", "unused"})
                var booking2 = bookingRepository.save(createBooking(
                        aiLab,
                        "Kavindu Silva",
                        "Faculty of Computing",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(13, 0),
                        LocalTime.of(16, 0),
                        "Cloud lab practical",
                        35,
                        BookingStatus.PENDING
                ));

                @SuppressWarnings({"null", "unused"})
                var ticket1 = maintenanceTicketRepository.save(createTicket(
                        projectorKit,
                        "Display failure",
                        "Projector lamp does not power on and the casing is overheating.",
                        "Media Unit",
                        MaintenancePriority.HIGH,
                        MaintenanceStatus.CLOSED,
                        "Technician Fernando"
                ));
                @SuppressWarnings({"null", "unused"})
                var ticket2 = maintenanceTicketRepository.save(createTicket(
                        meetingRoom,
                        "Audio issue",
                        "Ceiling microphone drops during hybrid calls after 15 minutes.",
                        "Business School",
                        MaintenancePriority.MEDIUM,
                        MaintenanceStatus.OPEN,
                        null
                ));
            }

            if (announcementRepository.count() == 0) {
                announcementRepository.save(createAnnouncement(
                        "Campus Facilities Maintenance Scheduled",
                        "General maintenance of lecture halls and labs will be conducted from April 15-20. Some resources may be temporarily unavailable.",
                        "Administration",
                        LocalDateTime.now().minusDays(2)
                ));
                announcementRepository.save(createAnnouncement(
                        "New Resource Booking Policy",
                        "Effective from next semester, all resource bookings must be made at least 48 hours in advance. Emergency bookings available upon approval.",
                        "Management",
                        LocalDateTime.now().minusDays(5)
                ));
                announcementRepository.save(createAnnouncement(
                        "System Updates & Maintenance",
                        "The campus resource management system will undergo scheduled maintenance on April 10 from 2:00 AM to 5:00 AM. Services will be unavailable during this period.",
                        "IT Support",
                        LocalDateTime.now().minusDays(7)
                ));
                announcementRepository.save(createAnnouncement(
                        "Equipment Upgrades Completed",
                        "All laboratory equipment has been upgraded to the latest models. Staff training sessions will be conducted throughout the week.",
                        "Facilities",
                        LocalDateTime.now().minusDays(10)
                ));
            }
        };
    }

    private Announcement createAnnouncement(String title, String content, String author, LocalDateTime createdAt) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setAuthor(author);
        announcement.setCreatedAt(createdAt);
        return announcement;
    }

    private Resource createResource(
            String name,
            ResourceType type,
            int capacity,
            String location,
            String availabilityWindow,
            ResourceStatus status,
            String description
    ) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setType(type);
        resource.setCapacity(capacity);
        resource.setLocation(location);
        resource.setAvailabilityWindow(availabilityWindow);
        resource.setStatus(status);
        resource.setDescription(description);
        return resource;
    }

    private Booking createBooking(
            Resource resource,
            String requestedBy,
            String department,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            String purpose,
            int expectedAttendees,
            BookingStatus status
    ) {
        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setRequestedBy(requestedBy);
        booking.setDepartment(department);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose(purpose);
        booking.setExpectedAttendees(expectedAttendees);
        booking.setStatus(status);
        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }

    private MaintenanceTicket createTicket(
            Resource resource,
            String issueType,
            String description,
            String reportedBy,
            MaintenancePriority priority,
            MaintenanceStatus status,
            String assignedTechnician
    ) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setResource(resource);
        ticket.setIssueType(issueType);
        ticket.setDescription(description);
        ticket.setReportedBy(reportedBy);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setAssignedTechnician(assignedTechnician);
        ticket.setCreatedAt(LocalDateTime.now().minusDays(1));
        ticket.setResolvedAt(status == MaintenanceStatus.RESOLVED ? LocalDateTime.now() : null);
        return ticket;
    }

    private void widenUserRoleColumn(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE app_users ALTER COLUMN role SET DATA TYPE VARCHAR(30)");
        } catch (Exception ignored) {
            // If the table is new or already widened, keep booting normally.
        }
    }

    private void widenResourceStatusColumn(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE resources ALTER COLUMN status SET DATA TYPE VARCHAR(40)");
        } catch (Exception ignored) {
            // If the table is new or already widened, keep booting normally.
        }
    }
}
