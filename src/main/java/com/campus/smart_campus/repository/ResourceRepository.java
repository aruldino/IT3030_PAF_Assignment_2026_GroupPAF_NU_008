package com.campus.smart_campus.repository;

import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.model.ResourceStatus;
import com.campus.smart_campus.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByType(ResourceType type);
    List<Resource> findByLocationContainingIgnoreCase(String location);
    List<Resource> findByCapacityGreaterThanEqual(int capacity);
    List<Resource> findByStatus(ResourceStatus status);
}
