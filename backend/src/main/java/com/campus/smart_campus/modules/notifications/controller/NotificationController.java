package com.campus.smart_campus.modules.notifications.controller;

import com.campus.smart_campus.modules.notifications.dto.NotificationPreferenceRequest;
import com.campus.smart_campus.modules.notifications.dto.NotificationResponse;
import com.campus.smart_campus.modules.notifications.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/notifications", "/notifications"})
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        return notificationService.getNotifications();
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }

    @GetMapping("/preferences")
    public NotificationPreferenceRequest getPreferences() {
        return notificationService.getPreferences();
    }

    @PutMapping("/preferences")
    public NotificationPreferenceRequest updatePreferences(@Valid @RequestBody NotificationPreferenceRequest request) {
        return notificationService.updatePreferences(request);
    }
}


