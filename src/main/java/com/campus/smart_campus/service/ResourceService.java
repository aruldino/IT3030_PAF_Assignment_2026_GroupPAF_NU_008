package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.ResourceRequest;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.ResourceType;
import com.campus.smart_campus.repository.BookingRepository;
import com.campus.smart_campus.repository.ResourceRepository;
import com.campus.smart_campus.repository.MaintenanceTicketRepository;
import java.util.Comparator;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceTicketRepository maintenanceTicketRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            MaintenanceTicketRepository maintenanceTicketRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.maintenanceTicketRepository = maintenanceTicketRepository;
    }

    public List<Resource> searchResources(ResourceType type, String location, Integer minCapacity, ResourceStatus status) {
        List<Resource> resources = resourceRepository.findAll();

        if (type != null) {
            resources = resources.stream()
                    .filter(resource -> resource.getType() == type)
                    .toList();
        }

        if (location != null && !location.isBlank()) {
            String normalizedLocation = location.trim().toLowerCase();
            resources = resources.stream()
                    .filter(resource -> resource.getLocation().toLowerCase().contains(normalizedLocation))
                    .toList();
        }

        if (minCapacity != null) {
            resources = resources.stream()
                    .filter(resource -> resource.getCapacity() >= minCapacity)
                    .toList();
        }

        if (status != null) {
            resources = resources.stream()
                    .filter(resource -> resource.getStatus() == status)
                    .toList();
        }

        return resources.stream()
                .sorted(Comparator.comparing(Resource::getName))
                .toList();
    }

    public Resource getResourceById(@NonNull Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found with id: " + id));
    }

    public Resource saveResource(@NonNull ResourceRequest request) {
        Resource resource = new Resource();
        apply(resource, request);
        return resourceRepository.save(resource);
    }

    public Resource updateResource(@NonNull Long id, @NonNull ResourceRequest request) {
        Resource existing = getResourceById(id);
        apply(existing, request);
        return resourceRepository.save(existing);
    }

    @Transactional
    public void deleteResource(@NonNull Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new NotFoundException("Resource not found with id: " + id);
        }

        bookingRepository.deleteByResourceId(id);
        maintenanceTicketRepository.deleteByResourceId(id);
        resourceRepository.deleteById(id);
        resourceRepository.flush();
    }

    private void apply(@NonNull Resource resource, @NonNull ResourceRequest request) {
        resource.setName(request.name().trim());
        resource.setType(request.type());
        resource.setCapacity(request.capacity());
        resource.setLocation(request.location().trim());
        resource.setAvailabilityWindow(request.availabilityWindow().trim());
        resource.setStatus(request.status());
        resource.setDescription(request.description().trim());
    }
}
