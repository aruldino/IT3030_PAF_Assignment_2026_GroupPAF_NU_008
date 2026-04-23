package com.smartcampus.service;

import com.smartcampus.dto.TicketCommentDTO;
import com.smartcampus.dto.TicketRequestDTO;
import com.smartcampus.dto.TicketResponseDTO;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.repository.TicketCommentRepository;
import com.smartcampus.repository.TicketRepository;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Incident ticket service — Member 3 responsibility.
 *
 * TODO (Member 3): Implement the full ticket workflow:
 *
 * STATUS WORKFLOW:
 *   OPEN ──► IN_PROGRESS (technician assigned)
 *   IN_PROGRESS ──► RESOLVED (technician adds resolution notes)
 *   RESOLVED ──► CLOSED (admin closes)
 *   OPEN/IN_PROGRESS ──► REJECTED (admin rejects with reason)
 *
 * ATTACHMENT HANDLING:
 *   - Store uploaded files on disk or cloud storage
 *   - Save TicketAttachment records with file metadata
 *   - Max 5MB per file, 15MB per request (configured in application.properties)
 *
 * COMMENT MANAGEMENT:
 *   - Both users and technicians can add comments
 *   - Comments are ordered by createdAt ASC
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TicketResponseDTO createTicket(String userId, TicketRequestDTO dto, List<MultipartFile> attachments) {
        // TODO (Member 3): Implement — create ticket, save attachments, notify admins
        throw new UnsupportedOperationException("TODO: Member 3 — implement createTicket()");
    }

    public List<TicketResponseDTO> getAllTickets() {
        // TODO (Member 3): Implement (admin/technician view)
        throw new UnsupportedOperationException("TODO: Member 3 — implement getAllTickets()");
    }

    public List<TicketResponseDTO> getMyTickets(String userId) {
        // TODO (Member 3): Implement (user's own tickets)
        throw new UnsupportedOperationException("TODO: Member 3 — implement getMyTickets()");
    }

    public TicketResponseDTO getTicketById(String id) {
        // TODO (Member 3): Implement
        throw new UnsupportedOperationException("TODO: Member 3 — implement getTicketById()");
    }

    public TicketResponseDTO updateTicketStatus(String id, TicketStatus newStatus, String notes) {
        // TODO (Member 3): Implement — validate status transition, notify user
        throw new UnsupportedOperationException("TODO: Member 3 — implement updateTicketStatus()");
    }

    public TicketResponseDTO assignTicket(String ticketId, String technicianId) {
        // TODO (Member 3): Implement — assign technician, set IN_PROGRESS, notify technician
        throw new UnsupportedOperationException("TODO: Member 3 — implement assignTicket()");
    }

    public TicketCommentDTO addComment(String ticketId, String userId, String content) {
        // TODO (Member 3): Implement
        throw new UnsupportedOperationException("TODO: Member 3 — implement addComment()");
    }

    public List<TicketCommentDTO> getComments(String ticketId) {
        // TODO (Member 3): Implement
        throw new UnsupportedOperationException("TODO: Member 3 — implement getComments()");
    }

    public void deleteTicket(String id) {
        // TODO (Member 3): Implement (admin only)
        throw new UnsupportedOperationException("TODO: Member 3 — implement deleteTicket()");
    }
}
