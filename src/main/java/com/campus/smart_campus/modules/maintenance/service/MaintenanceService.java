package com.campus.smart_campus.modules.maintenance.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.common.error.NotFoundException;
import com.campus.smart_campus.common.error.UnauthorizedException;
import com.campus.smart_campus.modules.auth.service.AuthService;
import com.campus.smart_campus.modules.auth.service.RoleAccess;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceCommentEditRequest;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceCommentResponse;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceRequest;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceSlaResponse;
import com.campus.smart_campus.modules.maintenance.model.MaintenancePriority;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceStatus;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceTicket;
import com.campus.smart_campus.modules.maintenance.repository.MaintenanceTicketRepository;
import com.campus.smart_campus.modules.resources.model.Resource;
import com.campus.smart_campus.modules.resources.service.ResourceService;
import com.campus.smart_campus.modules.users.model.AppUser;
import com.campus.smart_campus.modules.users.model.UserRole;
import com.campus.smart_campus.modules.users.repository.AppUserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class MaintenanceService {

    private final MaintenanceTicketRepository maintenanceTicketRepository;
    private final ResourceService resourceService;
    private final AppUserRepository appUserRepository;
    private final Map<Long, List<MaintenanceCommentResponse>> ticketComments = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> ticketAttachments = new ConcurrentHashMap<>();
    private final AtomicLong commentSequence = new AtomicLong(1);

    public MaintenanceService(
            MaintenanceTicketRepository maintenanceTicketRepository,
            ResourceService resourceService,
            AppUserRepository appUserRepository) {
        this.maintenanceTicketRepository = maintenanceTicketRepository;
        this.resourceService = resourceService;
        this.appUserRepository = appUserRepository;
    }

    public List<MaintenanceTicket> getTickets(MaintenanceStatus status) {
        List<MaintenanceTicket> tickets = status == null
                ? maintenanceTicketRepository.findAll()
                : maintenanceTicketRepository.findByStatusOrderByCreatedAtDesc(status);

        return tickets.stream()
                .sorted(Comparator.comparing(MaintenanceTicket::getCreatedAt).reversed())
                .toList();
    }

    public List<MaintenanceTicket> getTickets(MaintenanceStatus status, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        UserRole role = roleOf(currentUser);

        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return getTickets(status);
        }

        if (role == UserRole.TECHNICIAN) {
            return maintenanceTicketRepository.findAll().stream()
                    .filter(ticket -> isAssignedToCurrentUser(ticket, currentUser))
                    .filter(ticket -> status == null || ticket.getStatus() == status)
                    .sorted(Comparator.comparing(MaintenanceTicket::getCreatedAt).reversed())
                    .toList();
        }

        return maintenanceTicketRepository.findAll().stream()
                .filter(ticket -> isReportedByCurrentUser(ticket, currentUser))
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .sorted(Comparator.comparing(MaintenanceTicket::getCreatedAt).reversed())
                .toList();
    }

    public MaintenanceTicket createTicket(MaintenanceRequest request, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canCreateTickets(currentUser.getRole())) {
            throw new BusinessException("This role cannot create maintenance tickets.");
        }

        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setResource(resourceService.getResourceById(request.resourceId()));
        ticket.setIssueType(request.issueType().trim());
        ticket.setDescription(request.description().trim());
        ticket.setReportedBy(request.reportedBy().trim());
        ticket.setPriority(resolvePriority(request.issueType(), request.description(), request.priority()));
        ticket.setStatus(MaintenanceStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket updateStatus(@NonNull Long id, @NonNull MaintenanceStatus status,
            String assignedTechnician, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        UserRole role = roleOf(currentUser);
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));
        String normalizedAssignedTechnician = assignedTechnician == null ? null : assignedTechnician.trim();

        if (role == UserRole.TECHNICIAN
                && !isAssignedToCurrentUser(ticket, currentUser)) {
            throw new UnauthorizedException("Technicians can only update assigned tickets.");
        }

        if (!RoleAccess.canManageTickets(currentUser.getRole()) && role != UserRole.TECHNICIAN) {
            throw new UnauthorizedException("You do not have permission to update this ticket.");
        }

        if (role == UserRole.TECHNICIAN
                && normalizedAssignedTechnician != null
                && !matchesUser(normalizedAssignedTechnician, currentUser)) {
            throw new UnauthorizedException("Technicians cannot reassign tickets to another user.");
        }

        String effectiveAssignedTechnician = normalizedAssignedTechnician;
        if (status == MaintenanceStatus.IN_PROGRESS) {
            if (effectiveAssignedTechnician == null || effectiveAssignedTechnician.isBlank()) {
                effectiveAssignedTechnician = ticket.getAssignedTechnician();
            }
            if (effectiveAssignedTechnician == null || effectiveAssignedTechnician.isBlank()) {
                throw new BusinessException("Assigned technician is required before moving to IN_PROGRESS.");
            }
        }

        ticket.setStatus(status);
        if (effectiveAssignedTechnician != null) {
            ticket.setAssignedTechnician(effectiveAssignedTechnician);
        }
        if (status == MaintenanceStatus.IN_PROGRESS && ticket.getAssignedAt() == null) {
            ticket.setAssignedAt(LocalDateTime.now());
        }
        ticket.setResolvedAt(
                status == MaintenanceStatus.RESOLVED || status == MaintenanceStatus.CLOSED ? LocalDateTime.now()
                        : null);
        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket assignTechnician(@NonNull Long id, @NonNull String technician, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        UserRole role = roleOf(currentUser);
        if (role == UserRole.TECHNICIAN || role == UserRole.STUDENT) {
            throw new UnauthorizedException("Only administrators can assign technicians.");
        }

        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));

        ticket.setAssignedTechnician(technician.trim());
        if (ticket.getStatus() == MaintenanceStatus.OPEN) {
            ticket.setStatus(MaintenanceStatus.IN_PROGRESS);
            ticket.setAssignedAt(LocalDateTime.now());
        }
        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket changeStatus(@NonNull Long id, @NonNull MaintenanceStatus status, HttpSession session) {
        return updateStatus(id, status, null, session);
    }

    public List<MaintenanceCommentResponse> addComment(@NonNull Long id, @NonNull String author,
            @NonNull String comment, HttpSession session) {
        ensureTicketExists(id);
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canCreateTickets(currentUser.getRole())
                && !RoleAccess.canManageTickets(currentUser.getRole())) {
            throw new UnauthorizedException("You do not have permission to add comments.");
        }
        if (!RoleAccess.canManageResources(currentUser.getRole()) && !matchesUser(author.trim(), currentUser)) {
            throw new UnauthorizedException("You can only comment using your own account identity.");
        }
        MaintenanceCommentResponse entry = new MaintenanceCommentResponse(
                commentSequence.getAndIncrement(),
                author.trim(),
                comment.trim(),
                LocalDateTime.now(),
                LocalDateTime.now());
        ticketComments.computeIfAbsent(id, key -> new java.util.ArrayList<>()).add(entry);
        return ticketComments.get(id);
    }

    public List<MaintenanceCommentResponse> getComments(@NonNull Long id) {
        ensureTicketExists(id);
        return ticketComments.getOrDefault(id, List.of());
    }

    public MaintenanceCommentResponse editComment(
            @NonNull Long id,
            @NonNull Long commentId,
            @NonNull MaintenanceCommentEditRequest request,
            HttpSession session) {
        ensureTicketExists(id);
        AppUser currentUser = getCurrentUser(session);
        List<MaintenanceCommentResponse> comments = ticketComments.get(id);
        if (comments == null) {
            throw new NotFoundException("Comment not found.");
        }

        for (int index = 0; index < comments.size(); index++) {
            MaintenanceCommentResponse existing = comments.get(index);
            if (!existing.id().equals(commentId)) {
                continue;
            }

            if (!canManageComment(existing, currentUser)) {
                throw new BusinessException("You can only edit your own recent comments or use an admin account.");
            }

            MaintenanceCommentResponse updated = new MaintenanceCommentResponse(
                    existing.id(),
                    existing.author(),
                    request.comment().trim(),
                    existing.createdAt(),
                    LocalDateTime.now());
            comments.set(index, updated);
            return updated;
        }

        throw new NotFoundException("Comment not found.");
    }

    public List<MaintenanceCommentResponse> deleteComment(@NonNull Long id, @NonNull Long commentId,
            HttpSession session) {
        ensureTicketExists(id);
        AppUser currentUser = getCurrentUser(session);
        List<MaintenanceCommentResponse> comments = ticketComments.get(id);
        if (comments == null) {
            throw new NotFoundException("Comment not found.");
        }

        MaintenanceCommentResponse target = comments.stream()
                .filter(comment -> comment.id().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Comment not found."));

        if (!canManageComment(target, currentUser)) {
            throw new BusinessException("You can only delete your own recent comments or use an admin account.");
        }

        comments.removeIf(comment -> comment.id().equals(commentId));
        return comments;
    }

    public List<String> addAttachments(@NonNull Long id, @NonNull List<String> attachments, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canCreateTickets(currentUser.getRole())
                && !RoleAccess.canManageTickets(currentUser.getRole())) {
            throw new UnauthorizedException("You do not have permission to add attachments.");
        }

        ensureTicketExists(id);
        List<String> current = ticketAttachments.computeIfAbsent(id, key -> new java.util.ArrayList<>());
        if (current.size() + attachments.size() > 3) {
            throw new BusinessException("A ticket can have at most 3 attachments.");
        }
        current.addAll(attachments.stream().map(String::trim).filter(value -> !value.isBlank()).toList());
        return current;
    }

    public MaintenanceSlaResponse getSla(@NonNull Long id) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));

        LocalDateTime responsePoint = ticket.getAssignedAt() != null ? ticket.getAssignedAt() : LocalDateTime.now();
        LocalDateTime resolutionPoint = ticket.getResolvedAt() != null ? ticket.getResolvedAt() : LocalDateTime.now();
        long responseMinutes = Math.max(0, MaintenanceSlaResponse.minutesBetween(ticket.getCreatedAt(), responsePoint));
        long resolutionMinutes = Math.max(0,
                MaintenanceSlaResponse.minutesBetween(ticket.getCreatedAt(), resolutionPoint));

        return MaintenanceSlaResponse.of(
                ticket.getId(),
                ticket.getCreatedAt(),
                ticket.getResolvedAt(),
                responseMinutes,
                resolutionMinutes,
                responseMinutes <= 60,
                resolutionMinutes <= 8 * 60);
    }

    public MaintenanceTicket updateTicket(@NonNull Long id, MaintenanceRequest request, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canManageResources(currentUser.getRole())
                && !RoleAccess.canManageTickets(currentUser.getRole())) {
            throw new UnauthorizedException("You do not have permission to update this ticket.");
        }

        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));

        Resource resource = resourceService.getResourceById(request.resourceId());
        ticket.setResource(resource);
        ticket.setIssueType(request.issueType().trim());
        ticket.setDescription(request.description().trim());
        ticket.setReportedBy(request.reportedBy().trim());
        ticket.setPriority(resolvePriority(request.issueType(), request.description(), request.priority()));

        return maintenanceTicketRepository.save(ticket);
    }

    public void deleteTicket(@NonNull Long id, HttpSession session) {
        AppUser currentUser = getCurrentUser(session);
        if (!RoleAccess.canManageResources(currentUser.getRole())) {
            throw new UnauthorizedException("Only administrators can delete maintenance tickets.");
        }

        MaintenanceTicket ticket = maintenanceTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance ticket not found with id: " + id));
        maintenanceTicketRepository.delete(ticket);
    }

    private void ensureTicketExists(Long id) {
        if (!maintenanceTicketRepository.existsById(id)) {
            throw new NotFoundException("Maintenance ticket not found with id: " + id);
        }
    }

    private AppUser getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(AuthService.SESSION_USER_ID);
        if (userId == null) {
            throw new BusinessException("Please log in to continue.");
        }

        return appUserRepository.findById((Long) userId)
                .orElseThrow(() -> new NotFoundException("Authenticated user was not found."));
    }

    private boolean canManageComment(MaintenanceCommentResponse comment, AppUser currentUser) {
        String currentFullName = currentUser.getFullName().trim().toLowerCase();
        String currentEmail = currentUser.getEmail().trim().toLowerCase();
        String author = comment.author().trim().toLowerCase();
        boolean ownsComment = author.equals(currentFullName) || author.equals(currentEmail);
        boolean withinWindow = comment.createdAt().isAfter(LocalDateTime.now().minusMinutes(30));
        boolean privileged = RoleAccess.canManageResources(currentUser.getRole());
        return privileged || (ownsComment && withinWindow);
    }

    private UserRole roleOf(AppUser user) {
        return RoleAccess.normalize(user.getRole());
    }

    private boolean isAssignedToCurrentUser(MaintenanceTicket ticket, AppUser currentUser) {
        if (ticket.getAssignedTechnician() == null || ticket.getAssignedTechnician().isBlank()) {
            return false;
        }

        String assigned = ticket.getAssignedTechnician().trim().toLowerCase();
        String fullName = currentUser.getFullName().trim().toLowerCase();
        String email = currentUser.getEmail().trim().toLowerCase();
        return assigned.equals(fullName) || assigned.equals(email);
    }

    private boolean isReportedByCurrentUser(MaintenanceTicket ticket, AppUser currentUser) {
        String reportedBy = ticket.getReportedBy().trim().toLowerCase();
        String fullName = currentUser.getFullName().trim().toLowerCase();
        String email = currentUser.getEmail().trim().toLowerCase();
        return reportedBy.equals(fullName) || reportedBy.equals(email);
    }

    private boolean matchesUser(String value, AppUser currentUser) {
        String normalized = value.trim().toLowerCase();
        String fullName = currentUser.getFullName().trim().toLowerCase();
        String email = currentUser.getEmail().trim().toLowerCase();
        return normalized.equals(fullName) || normalized.equals(email);
    }

    private MaintenancePriority resolvePriority(String issueType, String description,
            MaintenancePriority requestedPriority) {
        String text = (issueType + " " + description).toLowerCase();
        if (containsAny(text, "fire", "smoke", "electrical", "sparking", "shutdown", "critical")) {
            return MaintenancePriority.CRITICAL;
        }
        if (containsAny(text, "broken", "urgent", "leak", "damage", "failed", "not working")) {
            return MaintenancePriority.HIGH;
        }
        return requestedPriority == null ? MaintenancePriority.MEDIUM : requestedPriority;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
