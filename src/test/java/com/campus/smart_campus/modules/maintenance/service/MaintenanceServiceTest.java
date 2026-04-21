package com.campus.smart_campus.modules.maintenance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.common.error.UnauthorizedException;
import com.campus.smart_campus.modules.auth.service.AuthService;
import com.campus.smart_campus.modules.maintenance.dto.MaintenanceCommentEditRequest;
import com.campus.smart_campus.modules.maintenance.model.MaintenancePriority;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceStatus;
import com.campus.smart_campus.modules.maintenance.model.MaintenanceTicket;
import com.campus.smart_campus.modules.maintenance.repository.MaintenanceTicketRepository;
import com.campus.smart_campus.modules.resources.model.Resource;
import com.campus.smart_campus.modules.resources.service.ResourceService;
import com.campus.smart_campus.modules.users.model.AppUser;
import com.campus.smart_campus.modules.users.model.UserRole;
import com.campus.smart_campus.modules.users.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceTicketRepository maintenanceTicketRepository;

    @Mock
    private ResourceService resourceService;

    @Mock
    private AppUserRepository appUserRepository;

    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService(maintenanceTicketRepository, resourceService, appUserRepository);
    }

    @Test
    void changeStatusKeepsAssignedTechnicianWhenNoNewAssigneeProvided() {
        AppUser admin = user(1L, "Admin One", "admin@campus.com", UserRole.ADMIN);
        MaintenanceTicket ticket = ticket(10L, "tech@campus.com", MaintenanceStatus.IN_PROGRESS);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(maintenanceTicketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(maintenanceTicketRepository.save(any(MaintenanceTicket.class)))
                .thenAnswer(invocation -> invocation.<MaintenanceTicket>getArgument(0));

        MaintenanceTicket updated = maintenanceService.changeStatus(10L, MaintenanceStatus.RESOLVED, sessionFor(1L));

        assertEquals("tech@campus.com", updated.getAssignedTechnician());
        assertEquals(MaintenanceStatus.RESOLVED, updated.getStatus());
        assertNotNull(updated.getResolvedAt());
    }

    @Test
    void technicianCannotReassignTicketThroughStatusUpdate() {
        AppUser technician = user(2L, "Tech User", "tech@campus.com", UserRole.TECHNICIAN);
        MaintenanceTicket ticket = ticket(11L, "tech@campus.com", MaintenanceStatus.IN_PROGRESS);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(technician));
        when(maintenanceTicketRepository.findById(11L)).thenReturn(Optional.of(ticket));

        assertThrows(
                UnauthorizedException.class,
                () -> maintenanceService.updateStatus(11L, MaintenanceStatus.IN_PROGRESS, "other@campus.com", sessionFor(2L)));
    }

    @Test
    void movingToInProgressWithoutAssigneeFails() {
        AppUser admin = user(3L, "Admin Two", "admin2@campus.com", UserRole.ADMIN);
        MaintenanceTicket openTicket = ticket(12L, null, MaintenanceStatus.OPEN);

        when(appUserRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(maintenanceTicketRepository.findById(12L)).thenReturn(Optional.of(openTicket));

        assertThrows(
                BusinessException.class,
                () -> maintenanceService.updateStatus(12L, MaintenanceStatus.IN_PROGRESS, null, sessionFor(3L)));
    }

    @Test
    void addCommentRejectsSpoofedAuthorForStudent() {
        AppUser student = user(4L, "Student One", "student@campus.com", UserRole.STUDENT);

        when(appUserRepository.findById(4L)).thenReturn(Optional.of(student));
        when(maintenanceTicketRepository.existsById(13L)).thenReturn(true);

        assertThrows(
                UnauthorizedException.class,
                () -> maintenanceService.addComment(13L, "another.user@campus.com", "Need update", sessionFor(4L)));
    }

    @Test
    void editCommentAllowsOwnerWithinWindow() {
        AppUser student = user(5L, "Student Two", "student2@campus.com", UserRole.STUDENT);

        when(appUserRepository.findById(5L)).thenReturn(Optional.of(student));
        when(maintenanceTicketRepository.existsById(14L)).thenReturn(true);

        maintenanceService.addComment(14L, "student2@campus.com", "Initial note", sessionFor(5L));
        var updated = maintenanceService.editComment(
                14L,
                1L,
                new MaintenanceCommentEditRequest("Edited note"),
                sessionFor(5L));

        assertEquals("Edited note", updated.comment());
    }

    @Test
    void closedTicketCannotBeReopened() {
        AppUser admin = user(6L, "Admin Three", "admin3@campus.com", UserRole.ADMIN);
        MaintenanceTicket closedTicket = ticket(15L, "tech@campus.com", MaintenanceStatus.CLOSED);

        when(appUserRepository.findById(6L)).thenReturn(Optional.of(admin));
        when(maintenanceTicketRepository.findById(15L)).thenReturn(Optional.of(closedTicket));

        assertThrows(
                BusinessException.class,
                () -> maintenanceService.updateStatus(15L, MaintenanceStatus.OPEN, null, sessionFor(6L)));
    }

    @Test
    void openTicketCannotBeResolvedDirectly() {
        AppUser admin = user(7L, "Admin Four", "admin4@campus.com", UserRole.ADMIN);
        MaintenanceTicket openTicket = ticket(16L, "tech@campus.com", MaintenanceStatus.OPEN);

        when(appUserRepository.findById(7L)).thenReturn(Optional.of(admin));
        when(maintenanceTicketRepository.findById(16L)).thenReturn(Optional.of(openTicket));

        assertThrows(
                BusinessException.class,
                () -> maintenanceService.updateStatus(16L, MaintenanceStatus.RESOLVED, null, sessionFor(7L)));
    }

    private MockHttpSession sessionFor(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.SESSION_USER_ID, userId);
        return session;
    }

    private AppUser user(Long id, String name, String email, UserRole role) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setFullName(name);
        appUser.setEmail(email);
        appUser.setRole(role);
        appUser.setActive(true);
        return appUser;
    }

    private MaintenanceTicket ticket(Long id, String assignedTechnician, MaintenanceStatus status) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setId(id);
        ticket.setResource(new Resource());
        ticket.setIssueType("Electrical");
        ticket.setDescription("Projector failed");
        ticket.setReportedBy("student@campus.com");
        ticket.setPriority(MaintenancePriority.MEDIUM);
        ticket.setStatus(status);
        ticket.setAssignedTechnician(assignedTechnician);
        ticket.setCreatedAt(LocalDateTime.now().minusHours(2));
        ticket.setAssignedAt(LocalDateTime.now().minusHours(1));
        return ticket;
    }
}
