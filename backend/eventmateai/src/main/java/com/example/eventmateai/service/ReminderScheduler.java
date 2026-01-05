package com.example.eventmateai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.eventmateai.model.Event;
import com.example.eventmateai.model.EventReminder;
import com.example.eventmateai.model.ReminderPreference;
import com.example.eventmateai.model.User;
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

    public ReminderScheduler(EventRepository eventRepository,
                             ReminderPreferenceRepository prefRepo,
                             EventReminderRepository reminderRepo,
                             EmailService emailService,
                             UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.prefRepo = prefRepo;
        this.reminderRepo = reminderRepo;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    // runs every minute (for testing)
    @Scheduled(cron = "0 * * * * *")
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusDays(3); // look 3 days ahead

        // 1) find upcoming events
        List<Event> upcomingEvents = eventRepository.findByDateTimeBetween(now, windowEnd);
        System.out.println("ReminderScheduler: running at " + now +
                ", upcomingEvents=" + upcomingEvents.size());

        for (Event event : upcomingEvents) {
            createReminderForEvent(event, 3);
            createReminderForEvent(event, 2);
        }

        // 2) send due reminders
        sendDueReminders();
    }

    private void createReminderForEvent(Event event, int daysBefore) {
        LocalDateTime remindAt = event.getDateTime().minusDays(daysBefore);
        if (remindAt.isBefore(LocalDateTime.now())) {
            return; // reminder time already passed
        }

        List<User> users = userRepository.findAll(); // later you can limit to booked users

        for (User user : users) {
            Optional<ReminderPreference> prefOpt = prefRepo.findByUserId(user.getId());
            if (prefOpt.isPresent() && !prefOpt.get().isEmailEnabled()) {
                continue; // this user disabled email
            }

            boolean exists = reminderRepo.existsByUserIdAndEventIdAndRemindAt(
                    user.getId(), event.getId(), remindAt);
            if (exists) {
                continue; // already have this reminder row
            }

            EventReminder r = new EventReminder();
            r.setUserId(user.getId());
            r.setEventId(event.getId());
            r.setRemindAt(remindAt);
            r.setOffsetLabel(daysBefore + " days before");
            r.setSent(false);
            reminderRepo.save(r);

            System.out.println("ReminderScheduler: created reminder for user "
                    + user.getId() + " event " + event.getId()
                    + " at " + remindAt);
        }
    }

    private void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<EventReminder> due =
                reminderRepo.findBySentFalseAndRemindAtBefore(now.plusMinutes(5));

        System.out.println("ReminderScheduler: due reminders count=" + due.size());

        for (EventReminder r : due) {
            Optional<User> userOpt = userRepository.findById(r.getUserId());
            Optional<Event> eventOpt = eventRepository.findById(r.getEventId());
            if (userOpt.isEmpty() || eventOpt.isEmpty()) continue;

            User user = userOpt.get();
            Event event = eventOpt.get();

            String subject = "Reminder: " + event.getTitle();
            String body = "This is a reminder for \"" + event.getTitle() + "\" on "
                    + event.getDateTime() + ".\n\n"
                    + "Reminder: " + r.getOffsetLabel() + ".\n\n"
                    + "EventMate AI";

            emailService.sendSimpleEmail(user.getEmail(), subject, body);

            r.setSent(true);
            reminderRepo.save(r);

            System.out.println("ReminderScheduler: sent email to "
                    + user.getEmail() + " for event " + event.getId());
        }
    }
}
