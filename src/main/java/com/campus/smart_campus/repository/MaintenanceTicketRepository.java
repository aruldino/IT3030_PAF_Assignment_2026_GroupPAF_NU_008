package com.campus.smart_campus.repository;

import com.campus.smart_campus.model.MaintenanceStatus;
import com.campus.smart_campus.model.MaintenanceTicket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {
    List<MaintenanceTicket> findByStatusOrderByCreatedAtDesc(MaintenanceStatus status);

    @Modifying
    @Query("delete from MaintenanceTicket t where t.resource.id = :resourceId")
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}
