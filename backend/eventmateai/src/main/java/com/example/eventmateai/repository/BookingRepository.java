package com.example.eventmateai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eventmateai.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEventId(Long eventId);
    List<Booking> findByUserId(Long userId);
}
