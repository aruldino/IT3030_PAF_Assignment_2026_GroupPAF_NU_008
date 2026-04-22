package com.campus.smart_campus.modules.resources.controller;

import com.campus.smart_campus.modules.resources.dto.ResourceRequest;
import com.campus.smart_campus.modules.resources.dto.ResourceAvailabilityResponse;
import com.campus.smart_campus.modules.resources.dto.ResourceUsageResponse;
import com.campus.smart_campus.common.error.UnauthorizedException;
import com.campus.smart_campus.modules.resources.model.Resource;
import com.campus.smart_campus.modules.resources.model.ResourceStatus;
import com.campus.smart_campus.modules.resources.model.ResourceType;
import com.campus.smart_campus.modules.users.model.UserRole;
import com.campus.smart_campus.modules.auth.service.AuthService;
import com.campus.smart_campus.modules.auth.service.RoleAccess;
import com.campus.smart_campus.modules.resources.service.ResourceService;
import jakarta.servlet.http.HttpSession;
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
    private final AuthService authService;

    public ResourceController(ResourceService resourceService, AuthService authService) {
        this.resourceService = resourceService;
        this.authService = authService;
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
    public Resource createResource(@Valid @RequestBody ResourceRequest resource, HttpSession session) {
        requireAdmin(session);
        logger.info("POST /api/resources - Creating resource: {}", resource.name());
        return resourceService.saveResource(resource);
    }

    @PostMapping(value = "/import/csv", consumes = {MediaType.TEXT_PLAIN_VALUE, "text/csv"})
    public List<Resource> importCsv(@RequestBody String csvContent, HttpSession session) {
        requireAdmin(session);
        logger.info("POST /api/resources/import/csv - Importing CSV resources");
        return resourceService.importResourcesFromCsv(csvContent);
    }

    @PutMapping("/{id}")
    public Resource updateResource(@PathVariable Long id, @Valid @RequestBody ResourceRequest resource, HttpSession session) {
        requireAdmin(session);
        logger.info("PUT /api/resources/{} - Updating resource", id);
        return resourceService.updateResource(id, resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResource(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        logger.info("DELETE /api/resources/{} - Deleting resource", id);
        resourceService.deleteResource(id);
        logger.info("DELETE /api/resources/{} - Resource deleted successfully", id);
        return ResponseEntity.ok(Map.of("message", "Resource deleted successfully"));
    }

    private void requireAdmin(HttpSession session) {
        UserRole role = authService.getCurrentUser(session).role();
        if (!RoleAccess.canManageResources(role)) {
            throw new UnauthorizedException("Only administrators can manage resources.");
        }
    }
}


