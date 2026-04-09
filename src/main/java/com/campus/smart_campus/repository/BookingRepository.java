package com.campus.smart_campus.repository;

import com.campus.smart_campus.model.Booking;
import com.campus.smart_campus.model.BookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatusOrderByBookingDateAscStartTimeAsc(BookingStatus status);

    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b
            where b.resource.id = :resourceId
              and b.bookingDate = :bookingDate
              and b.status = com.campus.smart_campus.model.BookingStatus.APPROVED
              and b.startTime < :endTime
              and b.endTime > :startTime
            """)
    boolean hasApprovedConflict(
            @Param("resourceId") Long resourceId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Modifying
    @Query("delete from Booking b where b.resource.id = :resourceId")
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}
