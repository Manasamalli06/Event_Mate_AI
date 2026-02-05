package com.example.eventmateai.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eventmateai.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    // existing: used by ReminderScheduler etc.
    List<Event> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    // NEW: count how many events a specific admin/user created
    long countByCreatedByUserId(Long createdByUserId);
}
