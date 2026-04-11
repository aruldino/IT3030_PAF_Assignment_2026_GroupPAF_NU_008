package com.campus.smart_campus.config;

import com.campus.smart_campus.model.Announcement;
import com.campus.smart_campus.model.Booking;
import com.campus.smart_campus.model.BookingStatus;
import com.campus.smart_campus.model.MaintenancePriority;
import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.MaintenanceTicket;
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
import org.springframework.jdbc.core.JdbcTemplate;

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
            "ICT Support"
    };

    private static final String[] REQUESTERS = {
            "Nethmi Perera",
            "Kavindu Silva",
            "Amaya Fernando",
            "Ravindu Jayasinghe",
            "Dasuni Wijesinghe",
            "Sahan Wickramaratne",
            "Tharushi Bandara",
            "Mihindu Gunasekara",
            "Yasith De Silva",
            "Piumi Rathnayake"
    };

    private static final String[] BOOKING_PURPOSES = {
            "Department workshop",
            "Guest lecture session",
            "Project presentation",
            "Lab practical class",
            "Committee meeting",
            "Orientation briefing",
            "Assessment preparation",
            "Student club event"
    };

    private static final String[] ISSUE_TYPES = {
            "Display failure",
            "Network outage",
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

    private static final String[] TECHNICIANS = {
            "Technician Fernando",
            "Technician Dilshan",
            "Technician Maleesha",
            "Technician Nisansala",
            "Technician Rashmi"
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
                log.info("Default sample users are ready (admin/student/staff/tech and related accounts).");
            }

            if (!seedDemoEnabled) {
                return;
            }

            try {
                seedBaselineOperationalData(resourceRepository, bookingRepository, maintenanceTicketRepository);

                ensureMinimumResources(resourceRepository, MIN_RESOURCE_COUNT);
                List<Resource> allResources = resourceRepository.findAll();
                ensureMinimumBookings(bookingRepository, allResources, MIN_BOOKING_COUNT);
                ensureMinimumTickets(maintenanceTicketRepository, allResources, MIN_TICKET_COUNT);
                backfillLegacyBookings(bookingRepository);
                backfillLegacyTickets(maintenanceTicketRepository);

                seedBaselineAnnouncementsIfMissing(announcementRepository);
                ensureMinimumAnnouncements(announcementRepository, MIN_ANNOUNCEMENT_COUNT);

                notificationService.syncAnnouncements(announcementRepository.findAll());
                log.info("Realistic sample data seed complete (resources/bookings/tickets/announcements).");
            } catch (Exception exception) {
                log.error(
                        "Sample data initialization failed. Application will continue with schema and seeded users.",
                        exception
                );
            }
        };
    }

    private void seedDefaultUsers(AuthService authService) {
        authService.createSeedUser("Campus Super Administrator", "superadmin@smartcampus.lk", "SuperAdmin@123", UserRole.SUPER_ADMIN);
        authService.createSeedUser("Campus Administrator", "admin@smartcampus.lk", "Admin@123", UserRole.ADMIN);
        authService.createSeedUser("Campus Student", "student@smartcampus.lk", "Student@123", UserRole.STUDENT);
        authService.createSeedUser("Campus Student Sample", "student1@smartcampus.lk", "Student@123", UserRole.STUDENT);
        authService.createSeedUser("Campus Staff", "staff@smartcampus.lk", "Staff@123", UserRole.STAFF);
        authService.createSeedUser("Campus User", "user@smartcampus.lk", "User@123", UserRole.USER);
        authService.createSeedUser("Campus Technician", "tech@smartcampus.lk", "Tech@123", UserRole.TECHNICIAN);
    }

    private void seedBaselineOperationalData(
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            MaintenanceTicketRepository maintenanceTicketRepository
    ) {
        if (resourceRepository.count() > 0) {
            return;
        }

        Resource aiLab = createResource(
                "AI Innovation Lab",
                ResourceType.LAB,
                40,
                "Engineering Building - Level 4",
                "Mon-Fri 08:00-18:00",
                ResourceStatus.ACTIVE,
                "High-performance computing lab for AI, analytics, and capstone sessions."
        );
        Resource auditorium = createResource(
                "Main Auditorium",
                ResourceType.LECTURE_HALL,
                220,
                "Administration Block",
                "Mon-Sat 08:00-20:00",
                ResourceStatus.ACTIVE,
                "Large event space for seminars, assessments, and orientation programs."
        );
        Resource meetingRoom = createResource(
                "Strategy Room B",
                ResourceType.MEETING_ROOM,
                18,
                "Business School - Level 2",
                "Mon-Fri 09:00-17:00",
                ResourceStatus.ACTIVE,
                "Hybrid meeting room with conferencing display and collaboration tools."
        );
        Resource projectorKit = createResource(
                "Projector Kit 07",
                ResourceType.EQUIPMENT,
                1,
                "Media Store",
                "Daily 08:30-16:30",
                ResourceStatus.MAINTENANCE,
                "Portable projector bundle currently pending preventive maintenance checks."
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
                "Lab practical class",
                35,
                BookingStatus.PENDING
        ));

        maintenanceTicketRepository.save(createTicket(
                projectorKit,
                "Display failure",
                "Projector lamp does not power on and unit overheats after 10 minutes.",
                "Media Unit",
                MaintenancePriority.HIGH,
                MaintenanceStatus.CLOSED,
                "Technician Fernando",
                LocalDateTime.now().minusDays(6).withHour(9).withMinute(15)
        ));
        maintenanceTicketRepository.save(createTicket(
                meetingRoom,
                "Audio disturbance",
                "Ceiling microphone drops during hybrid calls after approximately 15 minutes.",
                "Business School",
                MaintenancePriority.MEDIUM,
                MaintenanceStatus.OPEN,
                null,
                LocalDateTime.now().minusDays(2).withHour(11).withMinute(30)
        ));
    }

    private void seedBaselineAnnouncementsIfMissing(AnnouncementRepository announcementRepository) {
        if (announcementRepository.count() > 0) {
            return;
        }

        announcementRepository.save(createAnnouncement(
                "Campus Facilities Maintenance Scheduled",
                "General maintenance of lecture halls and labs will run from April 15 to April 20."
                        + " Some resources may be temporarily unavailable. Please check availability before booking.",
                "Administration",
                LocalDateTime.now().minusDays(2)
        ));
        announcementRepository.save(createAnnouncement(
                "New Resource Booking Policy",
                "From next semester onward, all regular resource bookings must be submitted at least 48 hours in advance."
                        + " Emergency bookings remain available with administrative approval.",
                "Management",
                LocalDateTime.now().minusDays(5)
        ));
        announcementRepository.save(createAnnouncement(
                "System Updates and Maintenance",
                "The operations platform will undergo scheduled maintenance this weekend from 02:00 to 05:00."
                        + " Services may be temporarily unavailable during this window.",
                "ICT Support",
                LocalDateTime.now().minusDays(7)
        ));
        announcementRepository.save(createAnnouncement(
                "Equipment Upgrades Completed",
                "Laboratory equipment upgrades are complete."
                        + " Departmental walkthrough sessions will be conducted during this week.",
                "Facilities",
                LocalDateTime.now().minusDays(10)
        ));
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
                ResourceStatus status = pickResourceStatus(index);
                Resource resource = createResource(
                        name,
                        type,
                        24 + ((index * 9) % 180),
                        "Block " + (char) ('A' + (index % 6)) + " - Level " + ((index % 5) + 1),
                        (index % 2 == 0) ? "Mon-Fri 08:00-18:00" : "Mon-Sat 09:00-17:00",
                        status,
                        "Operational resource seeded for realistic booking and maintenance workflows."
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
            LocalDate bookingDate = LocalDate.now().plusDays((offset % 28) - 9);
            LocalTime startTime = LocalTime.of(8 + (offset % 9), 0);

            int durationHours = resource.getType() == ResourceType.EQUIPMENT ? 1 : (2 + (offset % 2));
            LocalTime endTime = startTime.plusHours(durationHours);
            if (endTime.isAfter(LocalTime.of(21, 0))) {
                endTime = LocalTime.of(21, 0);
            }

            BookingStatus status = resolveBookingStatus(bookingDate, offset);
            int capacity = Math.max(1, resource.getCapacity());
            int expectedAttendees = Math.min(capacity, Math.max(1, 8 + ((offset * 11) % capacity)));

            Booking booking = createBooking(
                    resource,
                    REQUESTERS[offset % REQUESTERS.length],
                    DEPARTMENTS[offset % DEPARTMENTS.length],
                    bookingDate,
                    startTime,
                    endTime,
                    buildBookingPurpose(resource, offset),
                    expectedAttendees,
                    status
            );
            toCreate.add(booking);
            offset++;
        }

        bookingRepository.saveAll(toCreate);
    }

    private void ensureMinimumTickets(
            MaintenanceTicketRepository ticketRepository,
            List<Resource> resources,
            int minimumCount
    ) {
        long currentCount = ticketRepository.count();
        if (currentCount >= minimumCount || resources.isEmpty()) {
            return;
        }

        List<MaintenanceTicket> toCreate = new ArrayList<>();
        int offset = 1;
        while (currentCount + toCreate.size() < minimumCount) {
            Resource resource = resources.get(offset % resources.size());
            MaintenanceStatus status = resolveMaintenanceStatus(offset);
            String technician = status == MaintenanceStatus.OPEN
                    ? null
                    : TECHNICIANS[offset % TECHNICIANS.length];
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1 + (offset % 21)).withHour(8 + (offset % 8)).withMinute(15);

            MaintenanceTicket ticket = createTicket(
                    resource,
                    ISSUE_TYPES[offset % ISSUE_TYPES.length],
                    buildTicketDescription(resource, offset),
                    REPORTERS[offset % REPORTERS.length],
                    MaintenancePriority.values()[offset % MaintenancePriority.values().length],
                    status,
                    technician,
                    createdAt
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

        Set<String> existingTitles = new HashSet<>();
        announcementRepository.findAll().forEach(announcement -> existingTitles.add(announcement.getTitle()));

        List<Announcement> toCreate = new ArrayList<>();
        int offset = 1;
        while (currentCount + toCreate.size() < minimumCount) {
            String title = ANNOUNCEMENT_TITLES[offset % ANNOUNCEMENT_TITLES.length] + " " + offset;
            if (existingTitles.contains(title)) {
                offset++;
                continue;
            }

            String author = DEPARTMENTS[offset % DEPARTMENTS.length];
            Announcement announcement = createAnnouncement(
                    title,
                    buildAnnouncementContent(title, author, offset),
                    author,
                    LocalDateTime.now().minusDays(offset)
            );
            toCreate.add(announcement);
            existingTitles.add(title);
            offset++;
        }

        announcementRepository.saveAll(toCreate);
    }

    private void backfillLegacyBookings(BookingRepository bookingRepository) {
        List<Booking> bookings = bookingRepository.findAll();
        List<Booking> updates = new ArrayList<>();

        for (Booking booking : bookings) {
            boolean changed = false;

            if (booking.getBookingDate() != null) {
                if (booking.getAcademicYear() == null || booking.getAcademicYear().isBlank()) {
                    booking.setAcademicYear(resolveAcademicYear(booking.getBookingDate()));
                    changed = true;
                }

                if (booking.getSemester() == null || booking.getSemester().isBlank()) {
                    booking.setSemester(resolveSemester(booking.getBookingDate()));
                    changed = true;
                }
            }

            if (booking.getCreatedAt() == null && booking.getBookingDate() != null) {
                LocalDateTime proposedCreatedAt = booking.getBookingDate().atTime(8, 30).minusDays(5);
                LocalDateTime now = LocalDateTime.now();
                booking.setCreatedAt(proposedCreatedAt.isAfter(now) ? now.minusHours(2) : proposedCreatedAt);
                changed = true;
            }

            if (booking.getResource() != null && booking.getExpectedAttendees() > booking.getResource().getCapacity()) {
                booking.setExpectedAttendees(Math.max(1, booking.getResource().getCapacity()));
                changed = true;
            }

            if (booking.getExpectedAttendees() <= 0) {
                int fallbackAttendees = booking.getResource() == null ? 20 : Math.max(1, booking.getResource().getCapacity() / 2);
                booking.setExpectedAttendees(fallbackAttendees);
                changed = true;
            }

            if (booking.getPurpose() == null || booking.getPurpose().isBlank()) {
                Resource resource = booking.getResource();
                int seed = booking.getId() == null ? 1 : Math.abs(booking.getId().intValue());
                booking.setPurpose(buildBookingPurpose(resource == null ? createPlaceholderResource() : resource, seed));
                changed = true;
            }

            if (changed) {
                updates.add(booking);
            }
        }

        if (!updates.isEmpty()) {
            bookingRepository.saveAll(updates);
            log.info("Backfilled {} booking rows with realistic sample fields.", updates.size());
        }
    }

    private void backfillLegacyTickets(MaintenanceTicketRepository ticketRepository) {
        List<MaintenanceTicket> tickets = ticketRepository.findAll();
        List<MaintenanceTicket> updates = new ArrayList<>();

        for (MaintenanceTicket ticket : tickets) {
            boolean changed = false;

            if (ticket.getCreatedAt() == null) {
                ticket.setCreatedAt(LocalDateTime.now().minusDays(2));
                changed = true;
            }

            if (ticket.getStatus() == MaintenanceStatus.OPEN) {
                if (ticket.getAssignedAt() != null || ticket.getResolvedAt() != null || ticket.getAssignedTechnician() != null) {
                    ticket.setAssignedAt(null);
                    ticket.setResolvedAt(null);
                    ticket.setAssignedTechnician(null);
                    changed = true;
                }
            } else {
                if (ticket.getAssignedTechnician() == null || ticket.getAssignedTechnician().isBlank()) {
                    int seed = ticket.getId() == null ? 0 : Math.abs(ticket.getId().intValue());
                    ticket.setAssignedTechnician(TECHNICIANS[seed % TECHNICIANS.length]);
                    changed = true;
                }

                if (ticket.getAssignedAt() == null) {
                    ticket.setAssignedAt(ticket.getCreatedAt().plusHours(2));
                    changed = true;
                }

                if (ticket.getStatus() == MaintenanceStatus.IN_PROGRESS) {
                    if (ticket.getResolvedAt() != null) {
                        ticket.setResolvedAt(null);
                        changed = true;
                    }
                } else if (ticket.getResolvedAt() == null) {
                    long resolutionDays = ticket.getStatus() == MaintenanceStatus.CLOSED ? 2 : 1;
                    ticket.setResolvedAt(ticket.getAssignedAt().plusDays(resolutionDays));
                    changed = true;
                }
            }

            if (changed) {
                updates.add(ticket);
            }
        }

        if (!updates.isEmpty()) {
            ticketRepository.saveAll(updates);
            log.info("Backfilled {} maintenance ticket rows with realistic lifecycle fields.", updates.size());
        }
    }

    private Resource createPlaceholderResource() {
        Resource resource = new Resource();
        resource.setName("Campus Resource");
        resource.setCapacity(30);
        return resource;
    }

    private ResourceStatus pickResourceStatus(int seed) {
        int marker = seed % 10;
        if (marker < 6) {
            return ResourceStatus.ACTIVE;
        }
        if (marker < 8) {
            return ResourceStatus.MAINTENANCE;
        }
        if (marker == 8) {
            return ResourceStatus.UNDER_MAINTENANCE;
        }
        return ResourceStatus.OUT_OF_SERVICE;
    }

    private BookingStatus resolveBookingStatus(LocalDate bookingDate, int seed) {
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            if (seed % 6 == 0) {
                return BookingStatus.CANCELLED;
            }
            if (seed % 5 == 0) {
                return BookingStatus.REJECTED;
            }
            return BookingStatus.APPROVED;
        }

        if (bookingDate.isEqual(today)) {
            return seed % 3 == 0 ? BookingStatus.APPROVED : BookingStatus.PENDING;
        }

        return seed % 4 == 0 ? BookingStatus.APPROVED : BookingStatus.PENDING;
    }

    private MaintenanceStatus resolveMaintenanceStatus(int seed) {
        int marker = seed % 10;
        if (marker < 3) {
            return MaintenanceStatus.OPEN;
        }
        if (marker < 6) {
            return MaintenanceStatus.IN_PROGRESS;
        }
        if (marker < 8) {
            return MaintenanceStatus.RESOLVED;
        }
        return MaintenanceStatus.CLOSED;
    }

    private String buildBookingPurpose(Resource resource, int seed) {
        String base = BOOKING_PURPOSES[seed % BOOKING_PURPOSES.length];
        return base + " - " + resource.getName();
    }

    private String buildTicketDescription(Resource resource, int seed) {
        return "Issue reported for " + resource.getName()
                + ": " + ISSUE_TYPES[seed % ISSUE_TYPES.length].toLowerCase()
                + ". Needs on-site inspection and corrective action.";
    }

    private String buildAnnouncementContent(String title, String author, int offset) {
        return title + " from " + author
                + ": Operations update " + offset
                + " includes scheduling adjustments, maintenance checkpoints, and service availability notes for departments.";
    }

    private String resolveAcademicYear(LocalDate date) {
        int year = date.getYear();
        if (date.getMonthValue() >= 9) {
            return year + "/" + (year + 1);
        }
        return (year - 1) + "/" + year;
    }

    private String resolveSemester(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 9 || month <= 1) {
            return "Semester 1";
        }
        if (month <= 5) {
            return "Semester 2";
        }
        return "Mid Year Session";
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
        booking.setAcademicYear(resolveAcademicYear(bookingDate));
        booking.setSemester(resolveSemester(bookingDate));
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose(purpose);
        booking.setExpectedAttendees(expectedAttendees);
        booking.setStatus(status);

        LocalDateTime proposedCreatedAt = bookingDate.atTime(8, 30).minusDays(5);
        LocalDateTime now = LocalDateTime.now();
        booking.setCreatedAt(proposedCreatedAt.isAfter(now) ? now.minusHours(2) : proposedCreatedAt);

        return booking;
    }

    private MaintenanceTicket createTicket(
            Resource resource,
            String issueType,
            String description,
            String reportedBy,
            MaintenancePriority priority,
            MaintenanceStatus status,
            String assignedTechnician,
            LocalDateTime createdAt
    ) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setResource(resource);
        ticket.setIssueType(issueType);
        ticket.setDescription(description);
        ticket.setReportedBy(reportedBy);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setCreatedAt(createdAt);

        if (status == MaintenanceStatus.OPEN) {
            ticket.setAssignedTechnician(null);
            ticket.setAssignedAt(null);
            ticket.setResolvedAt(null);
            return ticket;
        }

        String technician = assignedTechnician == null || assignedTechnician.isBlank()
                ? TECHNICIANS[0]
                : assignedTechnician;
        LocalDateTime assignedAt = createdAt.plusHours(2);

        ticket.setAssignedTechnician(technician);
        ticket.setAssignedAt(assignedAt);

        if (status == MaintenanceStatus.RESOLVED) {
            ticket.setResolvedAt(assignedAt.plusDays(1));
            return ticket;
        }

        if (status == MaintenanceStatus.CLOSED) {
            ticket.setResolvedAt(assignedAt.plusDays(2));
            return ticket;
        }

        ticket.setResolvedAt(null);
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
