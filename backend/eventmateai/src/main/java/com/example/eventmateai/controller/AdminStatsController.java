package com.example.eventmateai.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.repository.EventRepository;

@RestController
@RequestMapping("/api/admin-stats")
@CrossOrigin(origins = "*")
public class AdminStatsController {

    private final EventRepository eventRepository;

    public AdminStatsController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public AdminStatsDto getStats(@RequestParam Long adminId) {
        AdminStatsDto dto = new AdminStatsDto();
        long eventsCreated = eventRepository.countByCreatedByUserId(adminId);
        dto.setEventsCreated(eventsCreated);
        dto.setTotalBookings(0L); // later replace with real bookings
        return dto;
    }

    public static class AdminStatsDto {
        private long eventsCreated;
        private long totalBookings;

        public long getEventsCreated() {
            return eventsCreated;
        }

        public void setEventsCreated(long eventsCreated) {
            this.eventsCreated = eventsCreated;
        }

        public long getTotalBookings() {
            return totalBookings;
        }

        public void setTotalBookings(long totalBookings) {
            this.totalBookings = totalBookings;
        }
    }
}
