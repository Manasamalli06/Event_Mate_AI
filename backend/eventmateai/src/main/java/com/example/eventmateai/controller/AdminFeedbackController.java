package com.example.eventmateai.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.dto.AdminFeedbackDto;
import com.example.eventmateai.model.ReviewStatus;
import com.example.eventmateai.service.AdminFeedbackService;

@RestController
@RequestMapping("/api/admin/feedback")
@CrossOrigin(origins = "*")
public class AdminFeedbackController {

    private final AdminFeedbackService feedbackService;

    public AdminFeedbackController(AdminFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public java.util.List<AdminFeedbackDto> getFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @GetMapping("/{id}")
    public AdminFeedbackDto getFeedback(@PathVariable Long id) {
        return feedbackService.getAdminFeedbackDto(id);
    }

    @PutMapping("/{id}/status")
    public AdminFeedbackDto updateStatus(@PathVariable Long id,
            @RequestParam ReviewStatus status) {
        return feedbackService.updateStatus(id, status);
    }
}
