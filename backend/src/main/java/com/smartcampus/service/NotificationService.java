package com.smartcampus.service;

import com.smartcampus.dto.NotificationDTO;
import com.smartcampus.model.Booking;
import com.smartcampus.repository.NotificationRepository;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notification service — Member 4 responsibility.
 *
 * TODO (Member 4): Implement notification creation and retrieval.
 *
 * Other services call createNotification() to push notifications to users.
 * Example callers:
 *   - BookingService.approveBooking() → notify applicant of approval
 *   - BookingService.rejectBooking()  → notify applicant of rejection
 *   - TicketService.assignTicket()    → notify assigned technician
 *   - TicketService.updateStatus()    → notify ticket creator of status change
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Creates and persists a notification for the specified user.
     * Called by other services — do NOT expose directly via HTTP.
     *
     * @param userId      recipient user ID
     * @param title       short notification title
     * @param message     full notification message
     * @param type        category string (e.g. "BOOKING_APPROVED", "TICKET_ASSIGNED")
     * @param referenceId ID of the related entity (booking ID, ticket ID, etc.) — nullable
     */
    public void createNotification(String userId, String title, String message,
                                   String type, String referenceId) {
        // TODO (Member 4): Implement — find user, create Notification entity, save
        throw new UnsupportedOperationException("TODO: Member 4 — implement createNotification()");
    }

    public List<NotificationDTO> getNotificationsForUser(String userId) {
        // TODO (Member 4): Implement
        throw new UnsupportedOperationException("TODO: Member 4 — implement getNotificationsForUser()");
    }

    public NotificationDTO markAsRead(String notificationId, String userId) {
        // TODO (Member 4): Implement — verify ownership, set isRead = true
        throw new UnsupportedOperationException("TODO: Member 4 — implement markAsRead()");
    }

    public void markAllAsRead(String userId) {
        // TODO (Member 4): Implement — bulk update for user's unread notifications
        throw new UnsupportedOperationException("TODO: Member 4 — implement markAllAsRead()");
    }

    public void deleteNotification(String notificationId, String userId) {
        // TODO (Member 4): Implement — verify ownership, then delete
        throw new UnsupportedOperationException("TODO: Member 4 — implement deleteNotification()");
    }

    public long getUnreadCount(String userId) {
        // TODO (Member 4): Implement
        throw new UnsupportedOperationException("TODO: Member 4 — implement getUnreadCount()");
    }

    public void notifyBookingApproved(String userId, Booking booking) {
    // Member 4 will implement
    }
    
    public void notifyBookingRejected(String userId, Booking booking) {
        // Member 4 will implement
    }
}
