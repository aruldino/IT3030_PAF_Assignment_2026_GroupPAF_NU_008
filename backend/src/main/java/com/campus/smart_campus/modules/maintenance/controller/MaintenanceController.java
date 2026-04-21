package com.campus.smart_campus.modules.maintenance.controller;

import com.campus.smart_campus.modules.maintenance.dto.MaintenanceAttachmentRequest;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceCommentEditRequest;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceCommentRequest;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceCommentResponse;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceRequest;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceSlaResponse;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceStatus;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceTicket;
import com.campus.smart_campus.modules.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/maintenance", "/tickets"})
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public List<MaintenanceTicket> getTickets(@RequestParam(required = false) MaintenanceStatus status, jakarta.servlet.http.HttpSession session) {
        return maintenanceService.getTickets(status, session);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceTicket createTicket(@Valid @RequestBody MaintenanceRequest request, jakarta.servlet.http.HttpSession session) {
        return maintenanceService.createTicket(request, session);
    }

    @PutMapping("/{id}")
    public MaintenanceTicket updateTicket(@PathVariable Long id, @Valid @RequestBody MaintenanceRequest request, jakarta.servlet.http.HttpSession session) {
        return maintenanceService.updateTicket(id, request, session);
    }

    @PutMapping("/{id}/assign")
    public MaintenanceTicket assignTechnician(
            @PathVariable Long id,
            @RequestParam String technician,
            jakarta.servlet.http.HttpSession session
    ) {
        return maintenanceService.assignTechnician(id, technician, session);
    }

    @PutMapping("/{id}/status")
    public MaintenanceTicket updateTicketStatus(
            @PathVariable Long id,
            @RequestParam MaintenanceStatus status,
            @RequestParam(required = false) String assignedTechnician,
            jakarta.servlet.http.HttpSession session
    ) {
        return maintenanceService.updateStatus(id, status, assignedTechnician, session);
    }

    @PostMapping("/{id}/comments")
    public List<MaintenanceCommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody MaintenanceCommentRequest request, jakarta.servlet.http.HttpSession session) {
        return maintenanceService.addComment(id, request.author(), request.comment(), session);
    }

    @PostMapping("/{id}/attachments")
    public List<String> addAttachments(@PathVariable Long id, @Valid @RequestBody MaintenanceAttachmentRequest request, jakarta.servlet.http.HttpSession session) {
        return maintenanceService.addAttachments(id, request.attachments(), session);
    }

    @GetMapping("/{id}/comments")
    public List<MaintenanceCommentResponse> getComments(@PathVariable Long id) {
        return maintenanceService.getComments(id);
    }

    @PutMapping("/{id}/comments/{commentId}")
    public MaintenanceCommentResponse editComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @Valid @RequestBody MaintenanceCommentEditRequest request,
            jakarta.servlet.http.HttpSession session
    ) {
        return maintenanceService.editComment(id, commentId, request, session);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public List<MaintenanceCommentResponse> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            jakarta.servlet.http.HttpSession session
    ) {
        return maintenanceService.deleteComment(id, commentId, session);
    }

    @GetMapping("/{id}/sla")
    public MaintenanceSlaResponse getSla(@PathVariable Long id) {
        return maintenanceService.getSla(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id, jakarta.servlet.http.HttpSession session) {
        maintenanceService.deleteTicket(id, session);
        return ResponseEntity.noContent().build();
    }
}


