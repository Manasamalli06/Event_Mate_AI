package com.example.eventmateai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.eventmateai.model.Booking;
import com.example.eventmateai.model.Event;
import com.example.eventmateai.model.EventReminder;
import com.example.eventmateai.model.ReminderPreference;
import com.example.eventmateai.model.User;
import com.example.eventmateai.repository.BookingRepository;
import com.example.eventmateai.repository.EventReminderRepository;
import com.example.eventmateai.repository.EventRepository;
import com.example.eventmateai.repository.ReminderPreferenceRepository;
import com.example.eventmateai.repository.UserRepository;

@Service
public class ReminderScheduler {

    private final EventRepository eventRepository;
    private final ReminderPreferenceRepository prefRepo;
    private final EventReminderRepository reminderRepo;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public ReminderScheduler(EventRepository eventRepository,
            ReminderPreferenceRepository prefRepo,
            EventReminderRepository reminderRepo,
            EmailService emailService,
            UserRepository userRepository,
            BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.prefRepo = prefRepo;
        this.reminderRepo = reminderRepo;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    // runs every minute
    @Scheduled(cron = "0 * * * * *")
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        // Look ahead 4 days to cover the 3-day reminder window
        LocalDateTime windowEnd = now.plusDays(4);

        // 1) Find upcoming events
        List<Event> upcomingEvents = eventRepository.findByDateTimeBetween(now, windowEnd);
        System.out.println("ReminderScheduler: running at " + now +
                ", upcomingEvents=" + upcomingEvents.size());

        for (Event event : upcomingEvents) {
            // 3 days before (3 * 24 * 60 minutes)
            createReminderForEvent(event, 4320, "3 days before");
            // 1 day before (24 * 60 minutes)
            createReminderForEvent(event, 1440, "1 day before");
            // 1 hour before (60 minutes)
            createReminderForEvent(event, 60, "1 hour before");
        }

        // 2) Send due reminders
        sendDueReminders();
    }

    private void createReminderForEvent(Event event, long minutesBuffer, String label) {
        LocalDateTime remindAt = event.getDateTime().minusMinutes(minutesBuffer);
        LocalDateTime now = LocalDateTime.now();

        // If reminder time is already passed, skip creating it
        if (remindAt.isBefore(now)) {
            return;
        }

        // Only create reminders for users who have booked this event
        List<Booking> bookings = bookingRepository.findByEventId(event.getId());

        for (Booking booking : bookings) {
            // Ensure booking is confirmed
            if (booking.getStatus() != null && !booking.getStatus().equalsIgnoreCase("CONFIRMED")) {
                continue;
            }

            Long userId = booking.getUserId();

            // Check if user has disabled email notifications
            Optional<ReminderPreference> prefOpt = prefRepo.findByUserId(userId);
            if (prefOpt.isPresent() && !prefOpt.get().isEmailEnabled()) {
                continue;
            }

            // Check if reminder already exists
            boolean exists = reminderRepo.existsByUserIdAndEventIdAndRemindAt(
                    userId, event.getId(), remindAt);
            if (exists) {
                continue;
            }

            // Schedule new reminder
            EventReminder r = new EventReminder();
            r.setUserId(userId);
            r.setEventId(event.getId());
            r.setRemindAt(remindAt);
            r.setOffsetLabel(label);
            r.setSent(false);
            reminderRepo.save(r);

            System.out.println("ReminderScheduler: created reminder for user "
                    + userId + " event " + event.getId()
                    + " (" + label + ") at " + remindAt);
        }
    }

    private void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        // Determine which reminders are due (scheduled time <= now + buffer)
        List<EventReminder> due = reminderRepo.findBySentFalseAndRemindAtBefore(now.plusMinutes(5));

        System.out.println("ReminderScheduler: due reminders count=" + due.size());

        for (EventReminder r : due) {
            Optional<User> userOpt = userRepository.findById(r.getUserId());
            Optional<Event> eventOpt = eventRepository.findById(r.getEventId());

            if (userOpt.isEmpty() || eventOpt.isEmpty()) {
                // If user or event deleted, mark sent effectively to ignore
                r.setSent(true);
                reminderRepo.save(r);
                continue;
            }

            User user = userOpt.get();
            Event event = eventOpt.get();

            String subject = "Reminder: " + event.getTitle();
            String body = "This is a reminder for \"" + event.getTitle() + "\" on "
                    + event.getDateTime() + ".\n\n"
                    + "Reminder: " + r.getOffsetLabel() + ".\n\n"
                    + "from EventMate AI";

            try {
                emailService.sendSimpleEmail(user.getEmail(), subject, body);
                r.setSent(true);
                reminderRepo.save(r);

                System.out.println("ReminderScheduler: sent email to "
                        + user.getEmail() + " for event " + event.getId());
            } catch (Exception e) {
                System.err.println(
                        "ReminderScheduler: Error sending email to " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }
}
