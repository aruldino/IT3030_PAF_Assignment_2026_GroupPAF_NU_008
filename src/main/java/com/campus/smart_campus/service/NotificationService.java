package com.campus.smart_campus.service;

import com.campus.smart_campus.dto.NotificationPreferenceRequest;
import com.campus.smart_campus.dto.NotificationResponse;
import com.campus.smart_campus.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, NotificationResponse> notifications = new ConcurrentHashMap<>();
    private volatile NotificationPreferenceRequest preferences = new NotificationPreferenceRequest(true, true, true, true);

    public NotificationService() {
        seed("Resource Update", "Main Auditorium is available for booking.", "RESOURCE");
        seed("Booking Alert", "A booking request is waiting for review.", "BOOKING");
        seed("Maintenance Update", "Projector Kit 07 is under maintenance.", "MAINTENANCE");
    }

    public List<NotificationResponse> getNotifications() {
        return notifications.values().stream()
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public NotificationResponse markRead(Long id) {
        NotificationResponse notification = notifications.get(id);
        if (notification == null) {
            throw new NotFoundException("Notification not found with id: " + id);
        }
        NotificationResponse updated = new NotificationResponse(
                notification.id(),
                notification.title(),
                notification.message(),
                notification.category(),
                true,
                notification.createdAt()
        );
        notifications.put(id, updated);
        return updated;
    }

    public NotificationPreferenceRequest getPreferences() {
        return preferences;
    }

    public NotificationPreferenceRequest updatePreferences(NotificationPreferenceRequest request) {
        preferences = request;
        return preferences;
    }

    private void seed(String title, String message, String category) {
        long id = sequence.getAndIncrement();
        notifications.put(id, new NotificationResponse(id, title, message, category, false, LocalDateTime.now().minusMinutes(id * 10)));
    }
}
