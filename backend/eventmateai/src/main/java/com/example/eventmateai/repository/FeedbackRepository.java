package com.example.eventmateai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eventmateai.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
