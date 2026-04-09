package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.DashboardSummary;
import com.campus.smart_campus.model.BookingStatus;
import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.repository.BookingRepository;
import com.campus.smart_campus.repository.MaintenanceTicketRepository;
import com.campus.smart_campus.repository.ResourceRepository;
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
                resourceRepository.findByStatus(ResourceStatus.OUT_OF_SERVICE).size(),
                bookingRepository.findByStatusOrderByBookingDateAscStartTimeAsc(BookingStatus.PENDING).size(),
                bookingRepository.findByStatusOrderByBookingDateAscStartTimeAsc(BookingStatus.APPROVED).size(),
                maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.OPEN).size()
                        + maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.IN_PROGRESS).size(),
                maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(MaintenanceStatus.RESOLVED).size()
        );
    }
}
