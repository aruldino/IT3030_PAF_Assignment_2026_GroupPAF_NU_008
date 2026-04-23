package com.smartcampus.service;

import com.smartcampus.dto.ResourceRequestDTO;
import com.smartcampus.dto.ResourceResponseDTO;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Resource service — Member 1 responsibility.
 *
 * TODO (Member 1): Implement all CRUD operations:
 *   - getAllResources(): Fetch all resources, map to ResourceResponseDTO list
 *   - getResourceById(Long id): Find by ID or throw ResourceNotFoundException
 *   - createResource(ResourceRequestDTO dto): Validate, map to entity, save, return DTO
 *   - updateResource(Long id, ResourceRequestDTO dto): Find, update fields, save, return DTO
 *   - deleteResource(Long id): Find by ID or throw ResourceNotFoundException, then delete
 *   - getResourcesByType(ResourceType type): Filter by type
 *   - getResourcesByStatus(ResourceStatus status): Filter by status
 *   - getResourcesByLocation(String location): Case-insensitive location search
 */
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public List<ResourceResponseDTO> getAllResources() {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement getAllResources()");
    }

    public ResourceResponseDTO getResourceById(String id) {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement getResourceById()");
    }

    public ResourceResponseDTO createResource(ResourceRequestDTO dto) {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement createResource()");
    }

    public ResourceResponseDTO updateResource(String id, ResourceRequestDTO dto) {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement updateResource()");
    }

    public void deleteResource(String id) {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement deleteResource()");
    }

    public List<ResourceResponseDTO> getResourcesByType(ResourceType type) {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement getResourcesByType()");
    }

    public List<ResourceResponseDTO> getResourcesByStatus(ResourceStatus status) {
        // TODO (Member 1): Implement
        throw new UnsupportedOperationException("TODO: Member 1 — implement getResourcesByStatus()");
    }
}
