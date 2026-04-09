package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.ResourceRequest;
import com.campus.smart_campus.exception.BusinessException;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.dto.BookingSlotResponse;
import com.campus.smart_campus.dto.ResourceAvailabilityResponse;
import com.campus.smart_campus.dto.ResourceUsageResponse;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.ResourceType;
import com.campus.smart_campus.repository.BookingRepository;
import com.campus.smart_campus.repository.ResourceRepository;
import com.campus.smart_campus.repository.MaintenanceTicketRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
        return searchResources(type, location, minCapacity, status, null);
    }

    public List<Resource> searchResources(
            ResourceType type,
            String location,
            Integer minCapacity,
            ResourceStatus status,
            String query
    ) {
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
                    .filter(resource -> isMatchingStatus(resource.getStatus(), status))
                    .toList();
        }

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<String> tokens = normalizedQuery.isBlank() ? List.of() : tokenize(normalizedQuery);

        Comparator<Resource> comparator = Comparator.comparing(Resource::getName);
        if (!normalizedQuery.isBlank()) {
            comparator = Comparator
                    .comparingInt((Resource resource) -> scoreResource(resource, normalizedQuery, tokens))
                    .reversed()
                    .thenComparing((Resource resource) -> resource.getStatus() == ResourceStatus.ACTIVE ? 0 : 1)
                    .thenComparing(Resource::getName);
        }

        return resources.stream()
                .sorted(comparator)
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

    public List<Resource> suggestResources(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return searchResources(null, null, null, null);
        }

        List<String> tokens = tokenize(normalized);
        return searchResources(null, null, null, null, normalized).stream()
                .sorted(Comparator
                        .comparingInt((Resource resource) -> scoreSuggestion(resource, normalized, tokens))
                        .reversed()
                        .thenComparing((Resource resource) -> resource.getStatus() == ResourceStatus.ACTIVE ? 0 : 1)
                        .thenComparing(Resource::getName))
                .limit(6)
                .toList();
    }

    public List<ResourceUsageResponse> getMostBookedResources() {
        Map<Long, Long> counts = new LinkedHashMap<>();
        bookingRepository.findAll().forEach(booking ->
                counts.merge(booking.getResource().getId(), 1L, Long::sum));

        return resourceRepository.findAll().stream()
                .map(resource -> new ResourceUsageResponse(
                        resource.getId(),
                        resource.getName(),
                        counts.getOrDefault(resource.getId(), 0L)
                ))
                .sorted(Comparator.comparingLong(ResourceUsageResponse::bookingCount).reversed()
                        .thenComparing(ResourceUsageResponse::resourceName))
                .limit(10)
                .toList();
    }

    public List<Resource> importResourcesFromCsv(@NonNull String csvContent) {
        if (csvContent.isBlank()) {
            throw new BusinessException("CSV file is empty.");
        }

        List<Resource> savedResources = new java.util.ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (firstLine && parts.length > 0 && "name".equalsIgnoreCase(parts[0].trim())) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                if (parts.length < 7) {
                    continue;
                }

                Resource resource = findByName(parts[0].trim()).orElseGet(Resource::new);
                resource.setName(parts[0].trim());
                resource.setType(ResourceType.valueOf(parts[1].trim().toUpperCase(Locale.ROOT)));
                resource.setCapacity(Integer.parseInt(parts[2].trim()));
                resource.setLocation(parts[3].trim());
                resource.setAvailabilityWindow(parts[4].trim());
                resource.setStatus(ResourceStatus.valueOf(parts[5].trim().toUpperCase(Locale.ROOT)));
                resource.setDescription(parts[6].trim());
                savedResources.add(resourceRepository.save(resource));
            }
        } catch (IOException ex) {
            throw new BusinessException("Unable to read CSV file.");
        }

        return savedResources;
    }

    public ResourceAvailabilityResponse getAvailability(@NonNull Long id) {
        Resource resource = getResourceById(id);
        List<BookingSlotResponse> bookedSlots = bookingRepository.findByResource_IdOrderByBookingDateAscStartTimeAsc(id)
                .stream()
                .map(booking -> new BookingSlotResponse(
                        booking.getId(),
                        booking.getBookingDate(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        booking.getStatus().name()
                ))
                .toList();

        return new ResourceAvailabilityResponse(
                resource.getId(),
                resource.getName(),
                resource.getAvailabilityWindow(),
                resource.getStatus(),
                bookedSlots
        );
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

    private int scoreSuggestion(Resource resource, String query, List<String> tokens) {
        return scoreResource(resource, query, tokens);
    }

    private int scoreResource(Resource resource, String query, List<String> tokens) {
        int score = 0;
        String name = resource.getName().toLowerCase(Locale.ROOT);
        String type = resource.getType().name().toLowerCase(Locale.ROOT);
        String location = resource.getLocation().toLowerCase(Locale.ROOT);
        String availability = resource.getAvailabilityWindow().toLowerCase(Locale.ROOT);
        String description = resource.getDescription().toLowerCase(Locale.ROOT);

        if (name.equals(query)) score += 20;
        if (name.startsWith(query)) score += 12;
        if (name.contains(query)) score += 8;
        if (type.contains(query) || matchesTypeAlias(resource.getType(), query)) score += 9;
        if (location.contains(query)) score += 7;
        if (availability.contains(query)) score += 4;
        if (description.contains(query)) score += 5;
        if (String.valueOf(resource.getCapacity()).equals(query)) score += 10;
        if (String.valueOf(resource.getCapacity()).contains(query)) score += 3;

        if (query.matches(".*\\d+.*")) {
            for (String token : tokens) {
                if (token.matches("\\d+")) {
                    int value = Integer.parseInt(token);
                    if (resource.getCapacity() >= value) {
                        score += 4;
                    }
                    if (resource.getCapacity() == value) {
                        score += 6;
                    }
                }
            }
        }

        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (name.contains(token)) score += 3;
            if (type.contains(token)) score += 4;
            if (location.contains(token)) score += 2;
            if (availability.contains(token)) score += 2;
            if (description.contains(token)) score += 2;
            if (matchesTypeAlias(resource.getType(), token)) score += 5;
            if (isCapacityIntent(token, resource.getCapacity())) score += 2;
        }

        if (resource.getStatus() == ResourceStatus.ACTIVE) {
            score += 2;
        } else if (isMaintenanceStatus(resource.getStatus())) {
            score -= 2;
        } else if (resource.getStatus() == ResourceStatus.OUT_OF_SERVICE) {
            score -= 4;
        }

        if (tokens.stream().anyMatch(token -> Set.of("available", "free", "open", "ready", "today").contains(token))) {
            score += resource.getStatus() == ResourceStatus.ACTIVE ? 5 : -5;
        }

        return score;
    }

    private boolean matchesTypeAlias(ResourceType type, String query) {
        return switch (type) {
            case LECTURE_HALL -> query.contains("hall") || query.contains("lecture") || query.contains("class") || query.contains("auditorium");
            case LAB -> query.contains("lab") || query.contains("laboratory") || query.contains("science");
            case MEETING_ROOM -> query.contains("meeting") || query.contains("conference") || query.contains("board");
            case EQUIPMENT -> query.contains("equipment") || query.contains("projector") || query.contains("device") || query.contains("kit");
        };
    }

    private boolean isCapacityIntent(String token, int capacity) {
        return switch (token) {
            case "small" -> capacity <= 30;
            case "medium" -> capacity > 30 && capacity <= 80;
            case "large", "big", "spacious" -> capacity > 80;
            default -> false;
        };
    }

    private boolean isMaintenanceStatus(ResourceStatus status) {
        return status == ResourceStatus.MAINTENANCE || status == ResourceStatus.UNDER_MAINTENANCE;
    }

    private boolean isMatchingStatus(ResourceStatus actual, ResourceStatus requested) {
        if (requested == null) {
            return true;
        }
        if (isMaintenanceStatus(requested)) {
            return isMaintenanceStatus(actual);
        }
        return actual == requested;
    }

    private List<String> tokenize(String query) {
        return java.util.Arrays.stream(query.split("[^a-z0-9]+"))
                .filter(token -> !token.isBlank())
                .toList();
    }

    private java.util.Optional<Resource> findByName(String name) {
        return resourceRepository.findAll().stream()
                .filter(resource -> resource.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
