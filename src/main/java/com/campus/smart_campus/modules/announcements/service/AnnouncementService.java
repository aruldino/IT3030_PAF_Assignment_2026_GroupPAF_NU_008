package com.campus.smart_campus.modules.announcements.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.campus.smart_campus.common.error.ForbiddenException;
import com.campus.smart_campus.common.error.NotFoundException;
import com.campus.smart_campus.modules.announcements.dto.AnnouncementRequest;
import com.campus.smart_campus.modules.announcements.model.Announcement;
import com.campus.smart_campus.modules.announcements.repository.AnnouncementRepository;
import com.campus.smart_campus.modules.auth.service.AuthService;
import com.campus.smart_campus.modules.auth.service.RoleAccess;
import com.campus.smart_campus.modules.notifications.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            AuthService authService,
            NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    public List<Announcement> getAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public Announcement createAnnouncement(AnnouncementRequest request, HttpSession session) {
        var currentUser = authService.getCurrentUser(session);
        if (!RoleAccess.canManageAnnouncements(currentUser.role())) {
            throw new ForbiddenException("Only administrators can post announcements.");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setAuthor(currentUser.fullName());
        announcement.setCreatedAt(LocalDateTime.now());
        Announcement saved = announcementRepository.save(announcement);
        notificationService.publishAnnouncement(saved);
        return saved;
    }

    public void deleteAnnouncement(Long id, HttpSession session) {
        var currentUser = authService.getCurrentUser(session);
        if (!RoleAccess.canManageAnnouncements(currentUser.role())) {
            throw new ForbiddenException("Only administrators can delete announcements.");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Announcement not found."));
        announcementRepository.delete(announcement);
        notificationService.publish(
                "Announcement removed: " + announcement.getTitle(),
                announcement.getContent(),
                "ANNOUNCEMENT");
    }

    public Announcement updateAnnouncement(Long id, AnnouncementRequest request, HttpSession session) {
        var currentUser = authService.getCurrentUser(session);
        if (!RoleAccess.canManageAnnouncements(currentUser.role())) {
            throw new ForbiddenException("Only administrators can edit announcements.");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Announcement not found."));
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        Announcement saved = announcementRepository.save(announcement);
        notificationService.publishAnnouncement(saved);
        return saved;
    }
}
