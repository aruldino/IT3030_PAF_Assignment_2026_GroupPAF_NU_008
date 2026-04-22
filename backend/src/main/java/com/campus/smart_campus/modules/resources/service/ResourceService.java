package com.campus.smart_campus.modules.resources.service;

import com.campus.smart_campus.modules.resources.dto.ResourceRequest;
import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.common.error.NotFoundException;
import com.campus.smart_campus.modules.bookings.dto.BookingSlotResponse;
import com.campus.smart_campus.modules.resources.dto.ResourceAvailabilityResponse;
import com.campus.smart_campus.modules.resources.dto.ResourceUsageResponse;
import com.campus.smart_campus.modules.resources.model.Resource;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;
import com.campus.smart_campus.modules.resources.model.ResourceType;
import com.campus.smart_campus.modules.bookings.repository.BookingRepository;
import com.campus.smart_campus.modules.resources.repository.ResourceRepository;
import com.campus.smart_campus.modules.maintenance.repository.MaintenanceTicketRepository;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
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
        validateSearchCapacity(minCapacity);
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

        List<Resource> savedResources = new ArrayList<>();
        List<String> rowErrors = new ArrayList<>();
        Character delimiter = null;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = stripBom(line).trim();
                if (line.isBlank()) {
                    continue;
                }

                if (delimiter == null) {
                    delimiter = detectDelimiter(line);
                }

                List<String> parts = parseCsvColumns(line, delimiter);

                if (lineNumber == 1 && isHeaderRow(parts)) {
                    continue;
                }

                if (parts.size() < 7) {
                    rowErrors.add("Line " + lineNumber + ": expected 7 columns (name,type,capacity,location,availabilityWindow,status,description).");
                    continue;
                }

                try {
                    String name = requiredValue(parts.get(0), "name", lineNumber);
                    ResourceType type = parseResourceType(parts.get(1), lineNumber);
                    int capacity = parseCapacity(parts.get(2), lineNumber);
                    String location = requiredValue(parts.get(3), "location", lineNumber);
                    String availabilityWindow = requiredValue(parts.get(4), "availabilityWindow", lineNumber);
                    ResourceStatus status = parseResourceStatus(parts.get(5), lineNumber);
                    String description = requiredValue(parts.get(6), "description", lineNumber);

                    Resource resource = findByName(name).orElseGet(Resource::new);
                    resource.setName(name);
                    resource.setType(type);
                    resource.setCapacity(capacity);
                    resource.setLocation(location);
                    resource.setAvailabilityWindow(availabilityWindow);
                    resource.setStatus(status);
                    resource.setDescription(description);

                    savedResources.add(resourceRepository.save(resource));
                } catch (IllegalArgumentException exception) {
                    rowErrors.add("Line " + lineNumber + ": " + exception.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("Unable to read CSV file.");
        }

        if (savedResources.isEmpty()) {
            if (!rowErrors.isEmpty()) {
                throw new BusinessException("CSV import failed. " + rowErrors.get(0));
            }
            throw new BusinessException("CSV file has no valid data rows.");
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
        validateRequestCapacity(request.capacity());
        resource.setName(sanitize(request.name()));
        resource.setType(request.type());
        resource.setCapacity(request.capacity());
        resource.setLocation(sanitize(request.location()));
        resource.setAvailabilityWindow(sanitize(request.availabilityWindow()));
        resource.setStatus(request.status());
        resource.setDescription(sanitize(request.description()));
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

    private String stripBom(String value) {
        if (value != null && !value.isEmpty() && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }

    private boolean isHeaderRow(List<String> parts) {
        return !parts.isEmpty() && "name".equalsIgnoreCase(stripBom(parts.get(0)).trim());
    }

    private char detectDelimiter(String line) {
        int commaCount = countUnquotedDelimiter(line, ',');
        int semicolonCount = countUnquotedDelimiter(line, ';');
        return semicolonCount > commaCount ? ';' : ',';
    }

    private int countUnquotedDelimiter(String line, char delimiter) {
        int count = 0;
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    i++;
                    continue;
                }
                inQuotes = !inQuotes;
                continue;
            }
            if (current == delimiter && !inQuotes) {
                count++;
            }
        }
        return count;
    }

    private List<String> parseCsvColumns(String line, char delimiter) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char value = line.charAt(i);

            if (value == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (value == delimiter && !inQuotes) {
                columns.add(current.toString().trim());
                current.setLength(0);
                continue;
            }

            current.append(value);
        }

        columns.add(current.toString().trim());
        return columns;
    }

    private String requiredValue(String value, String field, int lineNumber) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("missing required value for " + field + " at line " + lineNumber + ".");
        }
        return normalized;
    }

    private int parseCapacity(String value, int lineNumber) {
        String normalized = requiredValue(value, "capacity", lineNumber);
        try {
            int capacity = Integer.parseInt(normalized);
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity must be greater than zero.");
            }
            return capacity;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("capacity must be a valid number.");
        }
    }

    private ResourceType parseResourceType(String value, int lineNumber) {
        String normalized = normalizeToken(requiredValue(value, "type", lineNumber));
        String compact = normalized.replace("_", "");

        if ("LECTUREHALL".equals(compact)) {
            normalized = "LECTURE_HALL";
        } else if ("MEETINGROOM".equals(compact)) {
            normalized = "MEETING_ROOM";
        }

        try {
            return ResourceType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid resource type '" + value + "'.");
        }
    }

    private ResourceStatus parseResourceStatus(String value, int lineNumber) {
        String normalized = normalizeToken(requiredValue(value, "status", lineNumber));
        String compact = normalized.replace("_", "");

        if ("OUTOFSERVICE".equals(compact) || "OUTOFORDER".equals(compact) || "UNAVAILABLE".equals(compact)) {
            normalized = "OUT_OF_SERVICE";
        } else if ("UNDERMAINTENANCE".equals(compact)) {
            normalized = "UNDER_MAINTENANCE";
        } else if ("AVAILABLE".equals(compact)) {
            normalized = "ACTIVE";
        }

        try {
            return ResourceStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid resource status '" + value + "'.");
        }
    }

    private String normalizeToken(String value) {
        return value.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_')
                .replaceAll("_+", "_");
    }

    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    private void validateSearchCapacity(Integer minCapacity) {
        if (minCapacity != null && minCapacity < 0) {
            throw new BusinessException("Minimum capacity cannot be negative.");
        }
    }

    private void validateRequestCapacity(int capacity) {
        if (capacity <= 0) {
            throw new BusinessException("Capacity must be greater than zero.");
        }
    }
}


