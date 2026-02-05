package com.example.eventmateai.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.model.Booking;
import com.example.eventmateai.model.Event;
import com.example.eventmateai.model.EventReminder;
import com.example.eventmateai.model.ReminderPreference;
import com.example.eventmateai.repository.BookingRepository;
import com.example.eventmateai.repository.EventReminderRepository;
import com.example.eventmateai.repository.EventRepository;
import com.example.eventmateai.repository.ReminderPreferenceRepository;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*") // Allow all origins for dev compatibility
public class ReminderController {

    private final EventReminderRepository reminderRepo;
    private final ReminderPreferenceRepository prefRepo;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public ReminderController(EventReminderRepository reminderRepo,
            ReminderPreferenceRepository prefRepo,
            EventRepository eventRepository,
            BookingRepository bookingRepository) {
        this.reminderRepo = reminderRepo;
        this.prefRepo = prefRepo;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    // For event-reminders.html upcoming list
    // For event-reminders.html upcoming list
    // Modified to return ALL upcoming booked events, not just scheduled
    // notifications
    @GetMapping("/upcoming")
    public List<UpcomingReminderDto> getUpcoming(@RequestParam Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Get all bookings for user
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        // 2. Filter for future events and map
        return bookings.stream()
                .filter(b -> b.getStatus() == null || "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .map(b -> {
                    // Check event time
                    Optional<Event> evOpt = eventRepository.findById(b.getEventId());
                    if (evOpt.isEmpty())
                        return null;

                    Event ev = evOpt.get();

                    UpcomingReminderDto dto = new UpcomingReminderDto();
                    dto.setEventId(ev.getId());
                    dto.setEventTitle(ev.getTitle());

                    if (ev.getDateTime() == null) {
                        dto.setOffsetLabel("Date To Be Announced");
                    } else {
                        if (ev.getDateTime().isBefore(now))
                            return null; // Skip past events

                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                .ofPattern("MMM dd, yyyy h:mm a");
                        dto.setOffsetLabel(ev.getDateTime().format(formatter));
                    }

                    return dto;
                })
                .filter(dto -> dto != null)
                .sorted((d1, d2) -> d1.getOffsetLabel().compareTo(d2.getOffsetLabel())) // simple string sort
                                                                                        // approximation or improvements
                                                                                        // needed
                .toList();
    }

    // Preferences
    @GetMapping("/preferences")
    public ReminderPreference getPrefs(@RequestParam Long userId) {
        return prefRepo.findByUserId(userId)
                .orElseGet(() -> {
                    ReminderPreference p = new ReminderPreference();
                    p.setUserId(userId);
                    p.setEmailEnabled(false);
                    p.setPushEnabled(false);
                    return p;
                });
    }

    @PostMapping("/preferences")
    public ReminderPreference savePrefs(@RequestBody ReminderPreference pref) {
        ReminderPreference existing = prefRepo.findByUserId(pref.getUserId()).orElse(new ReminderPreference());
        existing.setUserId(pref.getUserId());
        existing.setEmailEnabled(pref.isEmailEnabled());
        existing.setPushEnabled(pref.isPushEnabled());
        return prefRepo.save(existing);
    }

    // simple DTO class inside controller
    public static class UpcomingReminderDto {
        private Long eventId;
        private String eventTitle;
        private String offsetLabel;

        public Long getEventId() {
            return eventId;
        }

        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }

        public String getEventTitle() {
            return eventTitle;
        }

        public void setEventTitle(String eventTitle) {
            this.eventTitle = eventTitle;
        }

        public String getOffsetLabel() {
            return offsetLabel;
        }

        public void setOffsetLabel(String offsetLabel) {
            this.offsetLabel = offsetLabel;
        }
    }
}
