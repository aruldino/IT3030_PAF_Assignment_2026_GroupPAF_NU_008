package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.AnnouncementRequest;
import com.campus.smart_campus.model.Announcement;
import com.campus.smart_campus.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public List<Announcement> getAnnouncements() {
        return announcementService.getAnnouncements();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Announcement createAnnouncement(@Valid @RequestBody AnnouncementRequest request, HttpSession session) {
        return announcementService.createAnnouncement(request, session);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id, HttpSession session) {
        announcementService.deleteAnnouncement(id, session);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Announcement updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request, HttpSession session) {
        return announcementService.updateAnnouncement(id, request, session);
    }
}
