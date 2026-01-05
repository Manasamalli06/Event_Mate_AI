package com.example.eventmateai.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eventmateai.model.EventReminder;

public interface EventReminderRepository extends JpaRepository<EventReminder, Long> {

    boolean existsByUserIdAndEventIdAndRemindAt(Long userId, Long eventId, LocalDateTime remindAt);

    List<EventReminder> findBySentFalseAndRemindAtBefore(LocalDateTime time);

    List<EventReminder> findByUserIdAndRemindAtBetweenOrderByRemindAtAsc(
            Long userId, LocalDateTime start, LocalDateTime end);
}
