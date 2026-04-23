package com.smartcampus.controller;

import com.smartcampus.dto.ResourceRequestDTO;
import com.smartcampus.dto.ResourceResponseDTO;
import com.smartcampus.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Resource REST controller — Member 1 responsibility.
 * Base path: /api/resources
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /** GET /api/resources — List all resources */
    @GetMapping
    public ResponseEntity<List<ResourceResponseDTO>> getAllResources() {
        // TODO (Member 1): return ResponseEntity.ok(resourceService.getAllResources());
        return ResponseEntity.ok().build();
    }

    /** GET /api/resources/{id} — Get resource by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponseDTO> getResourceById(@PathVariable String id) {
        // TODO (Member 1): return ResponseEntity.ok(resourceService.getResourceById(id));
        return ResponseEntity.ok().build();
    }

    /** POST /api/resources — Create a new resource (admin only) */
    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createResource(@Valid @RequestBody ResourceRequestDTO dto) {
        // TODO (Member 1): return ResponseEntity.status(201).body(resourceService.createResource(dto));
        return ResponseEntity.ok().build();
    }

    /** PUT /api/resources/{id} — Update an existing resource (admin only) */
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponseDTO> updateResource(@PathVariable String id,
                                                               @Valid @RequestBody ResourceRequestDTO dto) {
        // TODO (Member 1): return ResponseEntity.ok(resourceService.updateResource(id, dto));
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/resources/{id} — Delete a resource (admin only) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable String id) {
        // TODO (Member 1): resourceService.deleteResource(id); return ResponseEntity.noContent().build();
        return ResponseEntity.noContent().build();
    }
}
