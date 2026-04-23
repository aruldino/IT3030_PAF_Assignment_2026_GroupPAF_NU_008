package com.smartcampus.controller;

import com.smartcampus.dto.NotificationDTO;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification REST controller — Member 4 responsibility.
 * Base path: /api/notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /api/notifications — Get all notifications for current user */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        // TODO (Member 4): Extract userId from SecurityContext
        // return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
        return ResponseEntity.ok().build();
    }

    /** PATCH /api/notifications/{id}/read — Mark a notification as read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable String id) {
        // TODO (Member 4): Extract userId from SecurityContext
        // return ResponseEntity.ok(notificationService.markAsRead(id, userId));
        return ResponseEntity.ok().build();
    }

    /** PATCH /api/notifications/read-all — Mark all notifications as read */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        // TODO (Member 4): Extract userId from SecurityContext
        // notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/notifications/{id} — Delete a notification */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        // TODO (Member 4): Extract userId from SecurityContext
        // notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }
}
