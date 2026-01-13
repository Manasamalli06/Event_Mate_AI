package com.example.eventmateai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.eventmateai.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEventId(Long eventId);

    List<Booking> findByUserId(Long userId);

    // Count bookings for events created by a specific admin
    @Query(value = "SELECT COUNT(*) FROM bookings b JOIN events e ON b.event_id = e.id WHERE e.created_by_user_id = :adminId", nativeQuery = true)
    long countBookingsForAdmin(@Param("adminId") Long adminId);
}
