package com.campus.smart_campus.modules.announcements.repository;

import com.campus.smart_campus.modules.announcements.model.Announcement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByCreatedAtDesc();
}


