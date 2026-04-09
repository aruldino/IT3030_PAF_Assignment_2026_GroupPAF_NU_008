package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.MaintenanceRequest;
import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.MaintenanceTicket;
import com.campus.smart_campus.service.MaintenanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public List<MaintenanceTicket> getTickets(@RequestParam(required = false) MaintenanceStatus status) {
        return maintenanceService.getTickets(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceTicket createTicket(@Valid @RequestBody MaintenanceRequest request) {
        return maintenanceService.createTicket(request);
    }

    @PutMapping("/{id}")
    public MaintenanceTicket updateTicket(@PathVariable Long id, @Valid @RequestBody MaintenanceRequest request) {
        return maintenanceService.updateTicket(id, request);
    }

    @PatchMapping("/{id}/status")
    public MaintenanceTicket updateTicketStatus(
            @PathVariable Long id,
            @RequestParam MaintenanceStatus status,
            @RequestParam(required = false) String assignedTechnician
    ) {
        return maintenanceService.updateStatus(id, status, assignedTechnician);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        maintenanceService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
