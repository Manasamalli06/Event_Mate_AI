package com.example.eventmateai.dto;

import java.time.LocalDate;

public record FeedbackRequest(
        Long eventId,
        Long userId,
        int rating,
        String comments,
        LocalDate attendedDate
) {}
