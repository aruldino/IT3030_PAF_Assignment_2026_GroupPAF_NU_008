package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.ResourceRequest;
import com.campus.smart_campus.dto.ResourceAvailabilityResponse;
import com.campus.smart_campus.dto.ResourceUsageResponse;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.ResourceType;
import com.campus.smart_campus.service.ResourceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/resources", "/resources"})
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<Resource> getAllResources(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(required = false) String q
    ) {
        logger.info("GET /api/resources - Fetching resources");
        return resourceService.searchResources(type, location, capacity, status, q);
    }

    @GetMapping("/suggestions")
    public List<Resource> suggestResources(@RequestParam(required = false, defaultValue = "") String q) {
        logger.info("GET /api/resources/suggestions - Fetching suggestions");
        return resourceService.suggestResources(q);
    }

    @GetMapping("/analytics/most-booked")
    public List<ResourceUsageResponse> getMostBookedResources() {
        logger.info("GET /api/resources/analytics/most-booked - Fetching analytics");
        return resourceService.getMostBookedResources();
    }

    @GetMapping("/{id}")
    public Resource getResourceById(@PathVariable Long id) {
        logger.info("GET /api/resources/{} - Fetching resource", id);
        return resourceService.getResourceById(id);
    }

    @GetMapping("/{id}/availability")
    public ResourceAvailabilityResponse getAvailability(@PathVariable Long id) {
        logger.info("GET /api/resources/{}/availability - Fetching availability", id);
        return resourceService.getAvailability(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resource createResource(@Valid @RequestBody ResourceRequest resource) {
        logger.info("POST /api/resources - Creating resource: {}", resource.name());
        return resourceService.saveResource(resource);
    }

    @PostMapping(value = "/import/csv", consumes = {MediaType.TEXT_PLAIN_VALUE, "text/csv"})
    public List<Resource> importCsv(@RequestBody String csvContent) {
        logger.info("POST /api/resources/import/csv - Importing CSV resources");
        return resourceService.importResourcesFromCsv(csvContent);
    }

    @PutMapping("/{id}")
    public Resource updateResource(@PathVariable Long id, @Valid @RequestBody ResourceRequest resource) {
        logger.info("PUT /api/resources/{} - Updating resource", id);
        return resourceService.updateResource(id, resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResource(@PathVariable Long id) {
        logger.info("DELETE /api/resources/{} - Deleting resource", id);
        resourceService.deleteResource(id);
        logger.info("DELETE /api/resources/{} - Resource deleted successfully", id);
        return ResponseEntity.ok(Map.of("message", "Resource deleted successfully"));
    }
}
