package com.example.eventmateai.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.model.Booking;
import com.example.eventmateai.model.Event;
import com.example.eventmateai.repository.BookingRepository;
import com.example.eventmateai.repository.EventRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventController(EventRepository eventRepository,
            BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    // 2) Get all events (public)
    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // 1) Create new event (admin)
    @PostMapping("/create")
    public Event createEvent(@RequestBody CreateEventRequest req) {
        Event e = new Event();
        e.setTitle(req.getTitle());
        e.setDateTime(req.getDateTime() != null && !req.getDateTime().isEmpty() ? LocalDateTime.parse(req.getDateTime())
                : null);
        e.setEndDate(
                req.getEndDate() != null && !req.getEndDate().isEmpty() ? LocalDateTime.parse(req.getEndDate()) : null);
        e.setVenue(req.getVenue());
        e.setCategory(req.getCategory());
        e.setDescription(req.getDescription());
        e.setImageUrl(req.getImageUrl());
        e.setAvailableSeats(req.getAvailableSeats());
        e.setCostPerTicket(req.getCostPerTicket());
        e.setCurrency(req.getCurrency());
        e.setStatus(req.getStatus());
        e.setCreatedByUserId(req.getCreatedByUserId()); // Set creator
        return eventRepository.save(e);
    }

    // ...

    // 3) Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        eventRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 1b) Update existing event
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody Event updated) {

        return eventRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setDescription(updated.getDescription());
                    existing.setCategory(updated.getCategory());
                    existing.setDateTime(updated.getDateTime());
                    existing.setEndDate(updated.getEndDate());
                    existing.setAvailableSeats(updated.getAvailableSeats());
                    existing.setCostPerTicket(updated.getCostPerTicket());
                    existing.setCurrency(updated.getCurrency());
                    existing.setStatus(updated.getStatus());
                    existing.setImageUrl(updated.getImageUrl());
                    existing.setVenue(updated.getVenue());
                    Event saved = eventRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ... (rest of methods)

    // ... (GenerateDescriptionRequest)

    // DTO for create event request
    public static class CreateEventRequest {
        private String title;
        private String dateTime;
        private String endDate;
        private String venue;
        private String category;
        private String description;
        private String imageUrl;
        private Integer availableSeats;
        private Double costPerTicket;
        private String currency;
        private String status;
        private String location;
        private Long createdByUserId; // Add field

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getVenue() {
            return venue;
        }

        public void setVenue(String venue) {
            this.venue = venue;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Integer getAvailableSeats() {
            return availableSeats;
        }

        public void setAvailableSeats(Integer availableSeats) {
            this.availableSeats = availableSeats;
        }

        public Double getCostPerTicket() {
            return costPerTicket;
        }

        public void setCostPerTicket(Double costPerTicket) {
            this.costPerTicket = costPerTicket;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Long getCreatedByUserId() {
            return createdByUserId;
        }

        public void setCreatedByUserId(Long createdByUserId) {
            this.createdByUserId = createdByUserId;
        }
    }

    // DTO for generate description request
    public static class GenerateDescriptionRequest {
        private String title;
        private String category;
        private String venue;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getVenue() {
            return venue;
        }

        public void setVenue(String venue) {
            this.venue = venue;
        }

    }
}