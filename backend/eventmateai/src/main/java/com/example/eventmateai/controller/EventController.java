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

    // 1) Create new event (admin)
    @PostMapping("/create")
    public Event createEvent(@RequestBody CreateEventRequest req) {
        Event e = new Event();
        e.setTitle(req.getTitle());
        if (req.getDateTime() != null && !req.getDateTime().isEmpty()) {
            e.setDateTime(LocalDateTime.parse(req.getDateTime()));
        }
        e.setVenue(req.getVenue());
        e.setCategory(req.getCategory());
        e.setDescription(req.getDescription());
        e.setImageUrl(req.getImageUrl());
        e.setAvailableSeats(req.getAvailableSeats());
        e.setCostPerTicket(req.getCostPerTicket());
        e.setCurrency(req.getCurrency());
        e.setStatus(req.getStatus());
        return eventRepository.save(e);
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

    // 2) Get all events (for user Event List)
    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // 3) Get single event by id (for Event Details page)
    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    // 3b) Get booked seats for an event (for seat availability)
    // GET /api/events/{id}/seats -> { "bookedSeats": ["A1","A2", ...] }
    @GetMapping("/{id}/seats")
    public Map<String, List<String>> getBookedSeatsForEvent(@PathVariable Long id) {
        List<Booking> bookings = bookingRepository.findByEventId(id);
        List<String> bookedSeats = bookings.stream()
                .filter(b -> b.getSeats() != null)
                .flatMap(b -> Arrays.stream(b.getSeats().split(",")))
                .distinct()
                .collect(Collectors.toList());
        return Map.of("bookedSeats", bookedSeats);
    }

    // 4) Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        eventRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 5) Generate description with AI
    @PostMapping("/generate-description")
    public Map<String, String> generateDescriptionWithAI(@RequestBody GenerateDescriptionRequest req) {
        String title = req.getTitle() != null ? req.getTitle() : "this event";
        String category = req.getCategory() != null ? req.getCategory().toLowerCase() : "general";
        String venue = req.getVenue() != null ? req.getVenue() : "the selected venue";

        // Simple AI-like generation (can be replaced with actual AI API call)
        String description = "Join us for '" + title + "', an exciting " + category +
            " event taking place at " + venue + ". This promises to be an unforgettable experience " +
            "filled with engaging activities, networking opportunities, and memorable moments for all attendees.";

        return Map.of("description", description);
    }

    // DTO for create event request
    public static class CreateEventRequest {
        private String title;
        private String dateTime;
        private String venue;
        private String category;
        private String description;
        private String imageUrl;
        private Integer availableSeats;
        private Double costPerTicket;
        private String currency;
        private String status;
        private String location;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }

        public String getVenue() { return venue; }
        public void setVenue(String venue) { this.venue = venue; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

        public Double getCostPerTicket() { return costPerTicket; }
        public void setCostPerTicket(Double costPerTicket) { this.costPerTicket = costPerTicket; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    // DTO for generate description request
    public static class GenerateDescriptionRequest {
        private String title;
        private String category;
        private String venue;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getVenue() { return venue; }
        public void setVenue(String venue) { this.venue = venue; }

    }
}