package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.MaintenanceRequest;
import com.campus.smart_campus.exception.BusinessException;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.MaintenanceTicket;
import com.campus.smart_campus.model.Resource;
import com.campus.smart_campus.repository.MaintenanceTicketRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

@Service
public class MaintenanceService {

    private final MaintenanceTicketRepository maintenanceTicketRepository;
    private final ResourceService resourceService;
    private final Map<Long, List<String>> ticketComments = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> ticketAttachments = new ConcurrentHashMap<>();

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
        ticket.setResolvedAt(status == MaintenanceStatus.RESOLVED || status == MaintenanceStatus.CLOSED ? LocalDateTime.now() : null);
        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket assignTechnician(@NonNull Long id, @NonNull String technician) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));

        ticket.setAssignedTechnician(technician.trim());
        if (ticket.getStatus() == MaintenanceStatus.OPEN) {
            ticket.setStatus(MaintenanceStatus.IN_PROGRESS);
        }
        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket changeStatus(@NonNull Long id, @NonNull MaintenanceStatus status) {
        return updateStatus(id, status, null);
    }

    public List<String> addComment(@NonNull Long id, @NonNull String author, @NonNull String comment) {
        ensureTicketExists(id);
        ticketComments.computeIfAbsent(id, key -> new java.util.ArrayList<>())
                .add(author.trim() + ": " + comment.trim());
        return ticketComments.get(id);
    }

    public List<String> addAttachments(@NonNull Long id, @NonNull List<String> attachments) {
        ensureTicketExists(id);
        List<String> current = ticketAttachments.computeIfAbsent(id, key -> new java.util.ArrayList<>());
        if (current.size() + attachments.size() > 3) {
            throw new BusinessException("A ticket can have at most 3 attachments.");
        }
        current.addAll(attachments.stream().map(String::trim).filter(value -> !value.isBlank()).toList());
        return current;
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

    private void ensureTicketExists(Long id) {
        if (!maintenanceTicketRepository.existsById(id)) {
            throw new NotFoundException("Maintenance ticket not found with id: " + id);
        }
    }
}
