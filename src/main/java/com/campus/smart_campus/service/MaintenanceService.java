package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.MaintenanceRequest;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.MaintenanceTicket;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.repository.MaintenanceTicketRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

@Service
public class MaintenanceService {

    private final MaintenanceTicketRepository maintenanceTicketRepository;
    private final ResourceService resourceService;

    public MaintenanceService(MaintenanceTicketRepository maintenanceTicketRepository, ResourceService resourceService) {
        this.maintenanceTicketRepository = maintenanceTicketRepository;
        this.resourceService = resourceService;
    }

    public List<MaintenanceTicket> getTickets(MaintenanceStatus status) {
        List<MaintenanceTicket> tickets = status == null
                ? maintenanceTicketRepository.findAll()
                : maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(status);

        return tickets.stream()
                .sorted(Comparator.comparing(MaintenanceTicket::getCreatedAt).reversed())
                .toList();
    }

    public MaintenanceTicket createTicket(MaintenanceRequest request) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setResource(resourceService.getResourceById(request.resourceId()));
        ticket.setIssueType(request.issueType().trim());
        ticket.setDescription(request.description().trim());
        ticket.setReportedBy(request.reportedBy().trim());
        ticket.setPriority(request.priority());
        ticket.setStatus(MaintenanceStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket updateStatus(@NonNull Long id, @NonNull MaintenanceStatus status, String assignedTechnician) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));

        ticket.setStatus(status);
        ticket.setAssignedTechnician(assignedTechnician == null ? null : assignedTechnician.trim());
        ticket.setResolvedAt(status == MaintenanceStatus.RESOLVED ? LocalDateTime.now() : null);
        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket updateTicket(@NonNull Long id, MaintenanceRequest request) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));

        Resource resource = resourceService.getResourceById(request.resourceId());
        ticket.setResource(resource);
        ticket.setIssueType(request.issueType().trim());
        ticket.setDescription(request.description().trim());
        ticket.setReportedBy(request.reportedBy().trim());
        ticket.setPriority(request.priority());

        return maintenanceTicketRepository.save(ticket);
    }

    public void deleteTicket(@NonNull Long id) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));
        maintenanceTicketRepository.delete(ticket);
    }
}
