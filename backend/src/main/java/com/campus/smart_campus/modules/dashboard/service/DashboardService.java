package com.campus.smart_campus.modules.dashboard.service;

import com.campus.smart_campus.modules.dashboard.dto.DashboardSummary;
import com.campus.smart_campus.modules.bookings.model.BookingStatus;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceStatus;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;
import com.campus.smart_campus.modules.bookings.repository.BookingRepository;
import com.campus.smart_campus.modules.maintenance.repository.MaintenanceTicketRepository;
import com.campus.smart_campus.modules.resources.repository.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceTicketRepository maintenanceTicketRepository;

    public DashboardService(
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            MaintenanceTicketRepository maintenanceTicketRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.maintenanceTicketRepository = maintenanceTicketRepository;
    }

    public DashboardSummary getSummary() {
        return new DashboardSummary(
                resourceRepository.count(),
                resourceRepository.findByStatus(ResourceStatus.ACTIVE).size(),
                resourceRepository.findByStatus(ResourceStatus.OUT_OF_SERVICE).size()
                        + resourceRepository.findByStatus(ResourceStatus.MAINTENANCE).size(),
                bookingRepository.findByStatusOrderByBookingDateAscStartTimeAsc(BookingStatus.PENDING).size(),
                bookingRepository.findByStatusOrderByBookingDateAscStartTimeAsc(BookingStatus.APPROVED).size(),
                maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.OPEN).size()
                        + maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.IN_PROGRESS).size(),
                maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.RESOLVED).size()
                        + maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.CLOSED).size()
        );
    }
}


