package com.example.eventmateai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.dto.FeedbackRequest;
import com.example.eventmateai.model.Feedback;
import com.example.eventmateai.model.ReviewStatus;
import com.example.eventmateai.repository.FeedbackRepository;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest req) {
        if (req.rating() < 1 || req.rating() > 5) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }

        if (feedbackRepository.existsByEventIdAndUserId(req.eventId(), req.userId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Feedback already submitted");
        }

        Feedback f = new Feedback();
        f.setEventId(req.eventId());
        f.setUserId(req.userId());
        f.setRating(req.rating());
        f.setComments(req.comments());
        f.setStatus(ReviewStatus.PENDING);
        f.setAttendedDate(req.attendedDate());

        feedbackRepository.save(f);
        return ResponseEntity.ok().build();
    }
}
