package com.example.eventmateai.dto;

import java.time.LocalDate;

import com.example.eventmateai.model.ReviewStatus;

public record AdminFeedbackDto(
        Long id,
        Long eventId,
        String eventName,
        Long userId,
        String userName,
        String avatarUrl,
        LocalDate attendedDate,
        int rating,
        String comments,
        ReviewStatus status
) {}
