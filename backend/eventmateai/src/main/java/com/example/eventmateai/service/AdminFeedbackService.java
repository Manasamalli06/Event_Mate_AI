package com.example.eventmateai.service;

import com.example.eventmateai.dto.AdminFeedbackDto;
import com.example.eventmateai.model.Feedback;
import com.example.eventmateai.model.ReviewStatus;
import com.example.eventmateai.model.Event;
import com.example.eventmateai.model.User;
import com.example.eventmateai.repository.FeedbackRepository;
import com.example.eventmateai.repository.EventRepository;
import com.example.eventmateai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public AdminFeedbackService(FeedbackRepository feedbackRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminFeedbackDto getAdminFeedbackDto(Long id) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));

        Event event = eventRepository.findById(f.getEventId()).orElse(null);
        User user = userRepository.findById(f.getUserId()).orElse(null);

        String eventName = event != null ? event.getTitle() : null;
        String userName = user != null ? user.getFullName() : null; // adapt
        String avatarUrl = user != null ? user.getAvatarUrl() : null; // adapt

        return new AdminFeedbackDto(
                f.getId(),
                f.getEventId(),
                eventName,
                f.getUserId(),
                userName,
                avatarUrl,
                f.getAttendedDate(),
                f.getRating(),
                f.getComments(),
                f.getStatus());
    }

    @Transactional(readOnly = true)
    public java.util.List<AdminFeedbackDto> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(f -> getAdminFeedbackDto(f.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public AdminFeedbackDto updateStatus(Long id, ReviewStatus status) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));

        f.setStatus(status);
        feedbackRepository.save(f);

        return getAdminFeedbackDto(id);
    }
}
