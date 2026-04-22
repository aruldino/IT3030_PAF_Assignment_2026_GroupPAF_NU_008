package com.campus.smart_campus.modules.resources.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.modules.bookings.repository.BookingRepository;
import com.campus.smart_campus.modules.maintenance.repository.MaintenanceTicketRepository;
import com.campus.smart_campus.modules.resources.dto.ResourceRequest;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;
import com.campus.smart_campus.modules.resources.model.ResourceType;
import com.campus.smart_campus.modules.resources.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceServiceValidationTests {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MaintenanceTicketRepository maintenanceTicketRepository;

    @Test
    void searchResourcesRejectsNegativeMinCapacity() {
        ResourceService service = new ResourceService(resourceRepository, bookingRepository, maintenanceTicketRepository);

        assertThrows(
                BusinessException.class,
                () -> service.searchResources(null, null, -1, null)
        );
    }

    @Test
    void saveResourceRejectsNonPositiveCapacity() {
        ResourceService service = new ResourceService(resourceRepository, bookingRepository, maintenanceTicketRepository);
        ResourceRequest request = new ResourceRequest(
                "Main Auditorium",
                ResourceType.LECTURE_HALL,
                0,
                "Block A",
                "08:00-18:00",
                ResourceStatus.ACTIVE,
                "Primary large hall"
        );

        assertThrows(BusinessException.class, () -> service.saveResource(request));
    }
}
