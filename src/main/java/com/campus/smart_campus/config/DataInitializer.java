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
import com.campus.smart_campus.service.AuthService;
import com.campus.smart_campus.service.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

        private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final int MIN_RESOURCE_COUNT = 18;
    private static final int MIN_BOOKING_COUNT = 60;
    private static final int MIN_TICKET_COUNT = 36;
    private static final int MIN_ANNOUNCEMENT_COUNT = 24;

    private static final String[] RESOURCE_NAME_PREFIXES = {
            "Innovation Lab",
            "Smart Classroom",
            "Seminar Hall",
            "Conference Room",
            "Maker Space",
            "Research Lab",
            "Design Studio",
            "Media Suite",
            "Computer Lab",
            "Exam Hall"
    };

    private static final String[] DEPARTMENTS = {
            "Faculty of Computing",
            "Faculty of Engineering",
            "Faculty of Business",
            "Faculty of Science",
            "Student Affairs",
            "Administration",
            "Library Services",
            "IT Support"
    };

    private static final String[] REQUESTERS = {
            "Nethmi Perera",
            "Kavindu Silva",
            "Amaya Fernando",
            "Ravindu Jayasinghe",
            "Dasuni Wijesinghe",
            "Sahan Wickramaratne",
            "Tharushi Bandara",
            "Mihindu Gunasekara"
    };

    private static final String[] ISSUE_TYPES = {
            "Display failure",
            "Network issue",
            "Power fault",
            "Audio disturbance",
            "AC malfunction",
            "Furniture damage",
            "Software setup",
            "Peripheral replacement"
    };

    private static final String[] REPORTERS = {
            "Media Unit",
            "Facilities Team",
            "ICT Division",
            "Engineering Office",
            "Business School",
            "Science Faculty",
            "Student Services",
            "Admin Office"
    };

    private static final String[] ANNOUNCEMENT_TITLES = {
            "Weekly Operations Brief",
            "Resource Usage Snapshot",
            "Maintenance Progress Update",
            "Booking Policy Reminder",
            "Lab Access Schedule",
            "System Reliability Notice",
            "Facilities Readiness Report",
            "Campus Event Logistics"
    };

    @Bean
        @SuppressWarnings("null")
    CommandLineRunner seedDemoData(
            AnnouncementRepository announcementRepository,
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            MaintenanceTicketRepository maintenanceTicketRepository,
            JdbcTemplate jdbcTemplate,
            AuthService authService,
                        NotificationService notificationService,
                        @Value("${app.seed.users.enabled:true}") boolean seedUsersEnabled,
                        @Value("${app.seed.demo.enabled:true}") boolean seedDemoEnabled
    ) {
        return args -> {
            widenUserRoleColumn(jdbcTemplate);
            widenResourceStatusColumn(jdbcTemplate);

                        if (seedUsersEnabled) {
                                seedDefaultUsers(authService);
                                log.info("Default sample users are ready (admin/staff/tech and related accounts).");
                        }

                        if (!seedDemoEnabled) {
                                return;
                        }

                        try {
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

                resourceRepository.saveAll(List.of(aiLab, auditorium, meetingRoom, projectorKit));

                bookingRepository.save(createBooking(
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
                bookingRepository.save(createBooking(
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

                maintenanceTicketRepository.save(createTicket(
                        projectorKit,
                        "Display failure",
                        "Projector lamp does not power on and the casing is overheating.",
                        "Media Unit",
                        MaintenancePriority.HIGH,
                        MaintenanceStatus.CLOSED,
                        "Technician Fernando"
                ));
                maintenanceTicketRepository.save(createTicket(
                        meetingRoom,
                        "Audio issue",
                        "Ceiling microphone drops during hybrid calls after 15 minutes.",
                        "Business School",
                        MaintenancePriority.MEDIUM,
                        MaintenanceStatus.OPEN,
                        null
                ));
            }

                ensureMinimumResources(resourceRepository, MIN_RESOURCE_COUNT);
                List<Resource> allResources = resourceRepository.findAll();
                ensureMinimumBookings(bookingRepository, allResources, MIN_BOOKING_COUNT);
                ensureMinimumTickets(maintenanceTicketRepository, allResources, MIN_TICKET_COUNT);

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

                                ensureMinimumAnnouncements(announcementRepository, MIN_ANNOUNCEMENT_COUNT);

                                notificationService.syncAnnouncements(announcementRepository.findAll());
                        } catch (Exception exception) {
                                log.error("Demo data initialization failed. Application will continue with database schema and seeded users.", exception);
                        }
        };
    }

        private void seedDefaultUsers(AuthService authService) {
                authService.createSeedUser("Campus Super Administrator", "superadmin@smartcampus.lk", "SuperAdmin@123", UserRole.SUPER_ADMIN);
                authService.createSeedUser("Campus Administrator", "admin@smartcampus.lk", "Admin@123", UserRole.ADMIN);
                authService.createSeedUser("Campus Student", "student@smartcampus.lk", "Student@123", UserRole.STUDENT);
                authService.createSeedUser("Campus Staff", "staff@smartcampus.lk", "Staff@123", UserRole.STAFF);
                authService.createSeedUser("Campus User", "user@smartcampus.lk", "User@123", UserRole.USER);
                authService.createSeedUser("Campus Technician", "tech@smartcampus.lk", "Tech@123", UserRole.TECHNICIAN);
        }

        private void ensureMinimumResources(ResourceRepository resourceRepository, int minimumCount) {
                List<Resource> existingResources = resourceRepository.findAll();
                if (existingResources.size() >= minimumCount) {
                        return;
                }

                Set<String> existingNames = new HashSet<>();
                existingResources.forEach(resource -> existingNames.add(resource.getName()));

                List<Resource> toCreate = new ArrayList<>();
                int index = 1;
                while (existingResources.size() + toCreate.size() < minimumCount) {
                        String prefix = RESOURCE_NAME_PREFIXES[index % RESOURCE_NAME_PREFIXES.length];
                        String name = prefix + " " + index;
                        if (!existingNames.contains(name)) {
                                ResourceType type = ResourceType.values()[index % ResourceType.values().length];
                                ResourceStatus status = ResourceStatus.values()[index % ResourceStatus.values().length];
                                Resource resource = createResource(
                                                name,
                                                type,
                                                20 + ((index * 7) % 220),
                                                "Block " + (char) ('A' + (index % 6)) + " - Level " + ((index % 5) + 1),
                                                (index % 2 == 0) ? "Mon-Fri 08:00-18:00" : "Mon-Sat 09:00-17:00",
                                                status,
                                                "Auto-seeded resource for testing and demo workflows."
                                );
                                toCreate.add(resource);
                                existingNames.add(name);
                        }
                        index++;
                }

                resourceRepository.saveAll(toCreate);
        }

        private void ensureMinimumBookings(BookingRepository bookingRepository, List<Resource> resources, int minimumCount) {
                long currentCount = bookingRepository.count();
                if (currentCount >= minimumCount || resources.isEmpty()) {
                        return;
                }

                List<Booking> toCreate = new ArrayList<>();
                int offset = 1;
                while (currentCount + toCreate.size() < minimumCount) {
                        Resource resource = resources.get(offset % resources.size());
                        int startHour = 8 + (offset % 8);
                        Booking booking = createBooking(
                                        resource,
                                        REQUESTERS[offset % REQUESTERS.length],
                                        DEPARTMENTS[offset % DEPARTMENTS.length],
                                        LocalDate.now().plusDays((offset % 21) - 7),
                                        LocalTime.of(startHour, 0),
                                        LocalTime.of(Math.min(startHour + 2, 20), 0),
                                        "Scheduled session #" + offset,
                                        15 + (offset % 150),
                                        BookingStatus.values()[offset % BookingStatus.values().length]
                        );
                        toCreate.add(booking);
                        offset++;
                }

                bookingRepository.saveAll(toCreate);
        }

        private void ensureMinimumTickets(MaintenanceTicketRepository ticketRepository, List<Resource> resources, int minimumCount) {
                long currentCount = ticketRepository.count();
                if (currentCount >= minimumCount || resources.isEmpty()) {
                        return;
                }

                List<MaintenanceTicket> toCreate = new ArrayList<>();
                int offset = 1;
                while (currentCount + toCreate.size() < minimumCount) {
                        Resource resource = resources.get(offset % resources.size());
                        MaintenanceStatus status = MaintenanceStatus.values()[offset % MaintenanceStatus.values().length];
                        String technician = (status == MaintenanceStatus.OPEN) ? null : "Technician " + (char) ('A' + (offset % 6));
                        MaintenanceTicket ticket = createTicket(
                                        resource,
                                        ISSUE_TYPES[offset % ISSUE_TYPES.length],
                                        "Auto-seeded maintenance ticket for reliability testing #" + offset,
                                        REPORTERS[offset % REPORTERS.length],
                                        MaintenancePriority.values()[offset % MaintenancePriority.values().length],
                                        status,
                                        technician
                        );
                        toCreate.add(ticket);
                        offset++;
                }

                ticketRepository.saveAll(toCreate);
        }

        private void ensureMinimumAnnouncements(AnnouncementRepository announcementRepository, int minimumCount) {
                long currentCount = announcementRepository.count();
                if (currentCount >= minimumCount) {
                        return;
                }

                List<Announcement> toCreate = new ArrayList<>();
                int offset = 1;
                while (currentCount + toCreate.size() < minimumCount) {
                        String title = ANNOUNCEMENT_TITLES[offset % ANNOUNCEMENT_TITLES.length] + " " + offset;
                        String content = "Auto-seeded update to enrich dashboard activity and notification timelines (entry " + offset + ").";
                        String author = DEPARTMENTS[offset % DEPARTMENTS.length];
                        Announcement announcement = createAnnouncement(
                                        title,
                                        content,
                                        author,
                                        LocalDateTime.now().minusDays(offset)
                        );
                        toCreate.add(announcement);
                        offset++;
                }

                announcementRepository.saveAll(toCreate);
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
