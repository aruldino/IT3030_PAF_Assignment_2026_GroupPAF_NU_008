package com.campus.smart_campus.service;

import com.campus.smart_campus.model.Announcement;
import com.campus.smart_campus.dto.NotificationPreferenceRequest;
import com.campus.smart_campus.dto.NotificationResponse;
import com.campus.smart_campus.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, NotificationResponse> notifications = new ConcurrentHashMap<>();
    private final Set<String> notificationKeys = ConcurrentHashMap.newKeySet();
    private volatile NotificationPreferenceRequest preferences = new NotificationPreferenceRequest(true, true, true, true);

    public NotificationService() {
        publish("Resource Update", "Main Auditorium is available for booking.", "RESOURCE");
        publish("Booking Alert", "A booking request is waiting for review.", "BOOKING");
        publish("Maintenance Update", "Projector Kit 07 is under maintenance.", "MAINTENANCE");
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

    public NotificationResponse publish(String title, String message, String category) {
        return store(title, message, category, LocalDateTime.now(), false);
    }

    public NotificationResponse publishAnnouncement(Announcement announcement) {
        String title = "Announcement: " + announcement.getTitle();
        String message = announcement.getContent() + " - " + announcement.getAuthor();
        return store(title, message, "ANNOUNCEMENT", announcement.getCreatedAt(), false);
    }

    public void syncAnnouncements(List<Announcement> announcements) {
        announcements.forEach(this::publishAnnouncement);
    }

    private NotificationResponse store(String title, String message, String category, LocalDateTime createdAt, boolean read) {
        String key = category + "|" + title + "|" + message;
        if (!notificationKeys.add(key)) {
            return notifications.values().stream()
                    .filter(notification -> notification.category().equals(category))
                    .filter(notification -> notification.title().equals(title))
                    .filter(notification -> notification.message().equals(message))
                    .findFirst()
                    .orElseGet(() -> new NotificationResponse(0L, title, message, category, read, createdAt));
        }

        long id = sequence.getAndIncrement();
        NotificationResponse notification = new NotificationResponse(id, title, message, category, read, createdAt);
        notifications.put(id, notification);
        return notification;
    }
}
