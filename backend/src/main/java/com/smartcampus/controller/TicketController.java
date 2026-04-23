package com.smartcampus.controller;

import com.smartcampus.dto.TicketCommentDTO;
import com.smartcampus.dto.TicketRequestDTO;
import com.smartcampus.dto.TicketResponseDTO;
import com.smartcampus.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ticket REST controller — Member 3 responsibility.
 * Base path: /api/tickets
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /** POST /api/tickets — Submit a new incident ticket */
    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketRequestDTO dto) {
        // TODO (Member 3): Extract userId from SecurityContext, handle multipart attachments
        // return ResponseEntity.status(201).body(ticketService.createTicket(userId, dto, attachments));
        return ResponseEntity.ok().build();
    }

    /** GET /api/tickets — Get all tickets (admin/technician) */
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        // TODO (Member 3): return ResponseEntity.ok(ticketService.getAllTickets());
        return ResponseEntity.ok().build();
    }

    /** GET /api/tickets/my — Get current user's tickets */
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        // TODO (Member 3): Extract userId from SecurityContext
        return ResponseEntity.ok().build();
    }

    /** GET /api/tickets/{id} — Get ticket by ID */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable String id) {
        // TODO (Member 3): return ResponseEntity.ok(ticketService.getTicketById(id));
        return ResponseEntity.ok().build();
    }

    /** PATCH /api/tickets/{id}/status — Update ticket status */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> updateTicketStatus(@PathVariable String id,
                                                                 @RequestBody String status) {
        // TODO (Member 3): Parse status enum, call ticketService.updateTicketStatus(id, status, notes)
        return ResponseEntity.ok().build();
    }

    /** PUT /api/tickets/{id}/assign — Assign ticket to a technician */
    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable String id,
                                                           @RequestParam String technicianId) {
        // TODO (Member 3): return ResponseEntity.ok(ticketService.assignTicket(id, technicianId));
        return ResponseEntity.ok().build();
    }

    /** POST /api/tickets/{id}/comments — Add a comment to a ticket */
    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentDTO> addComment(@PathVariable String id,
                                                        @RequestBody String content) {
        // TODO (Member 3): Extract userId from SecurityContext
        return ResponseEntity.ok().build();
    }

    /** GET /api/tickets/{id}/comments — Get comments for a ticket */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TicketCommentDTO>> getComments(@PathVariable String id) {
        // TODO (Member 3): return ResponseEntity.ok(ticketService.getComments(id));
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/tickets/{id} — Delete a ticket (admin only) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        // TODO (Member 3): ticketService.deleteTicket(id); return ResponseEntity.noContent().build();
        return ResponseEntity.noContent().build();
    }
}
