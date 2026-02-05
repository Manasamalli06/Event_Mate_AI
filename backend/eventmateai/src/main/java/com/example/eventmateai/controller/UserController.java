package com.example.eventmateai.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.model.User;
import com.example.eventmateai.repository.UserRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final com.example.eventmateai.repository.BookingRepository bookingRepository;
    private final com.example.eventmateai.repository.EventRepository eventRepository;

    public UserController(UserRepository userRepository,
            com.example.eventmateai.repository.BookingRepository bookingRepository,
            com.example.eventmateai.repository.EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User u = user.get();
            u.setPassword(null);
            return ResponseEntity.ok(buildResponse(u));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            // Update fields if they are not null
            if (updatedUser.getFullName() != null)
                existingUser.setFullName(updatedUser.getFullName());
            if (updatedUser.getEmail() != null)
                existingUser.setEmail(updatedUser.getEmail());
            if (updatedUser.getAvatarUrl() != null)
                existingUser.setAvatarUrl(updatedUser.getAvatarUrl());
            if (updatedUser.getBio() != null)
                existingUser.setBio(updatedUser.getBio());
            if (updatedUser.getPhoneNumber() != null)
                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            if (updatedUser.getLocation() != null)
                existingUser.setLocation(updatedUser.getLocation());

            System.out.println("Saving updated profile for user ID " + id + " to MySQL database...");
            userRepository.save(existingUser);
            System.out.println("Profile saved successfully.");

            // clear password before returning
            existingUser.setPassword(null);

            return ResponseEntity.ok(buildResponse(existingUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private UserProfileResponse buildResponse(User user) {
        int bookingCount = 0;
        long eventsCreated = 0;
        long totalBookingsReceived = 0;

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            eventsCreated = eventRepository.countByCreatedByUserId(user.getId());
            totalBookingsReceived = bookingRepository.countBookingsForAdmin(user.getId());
        } else {
            // Regular user stats: bookings made
            bookingCount = bookingRepository.findByUserId(user.getId()).size();
        }

        return new UserProfileResponse(user, bookingCount, eventsCreated, totalBookingsReceived);
    }

    // DTO
    static class UserProfileResponse {
        private User user;
        private int bookingCount; // For regular users (bookings made)
        private int eventsCount; // For regular users (events attended - placeholder)

        private long eventsCreated; // For admins
        private long bookingsReceived; // For admins (total bookings on their events)

        public UserProfileResponse(User user, int bookingCount, long eventsCreated, long bookingsReceived) {
            this.user = user;
            this.bookingCount = bookingCount;
            this.eventsCount = bookingCount; // defaulting eventsCount to bookingCount for users
            this.eventsCreated = eventsCreated;
            this.bookingsReceived = bookingsReceived;
        }

        public User getUser() {
            return user;
        }

        public int getBookingCount() {
            return bookingCount;
        }

        public int getEventsCount() {
            return eventsCount;
        }

        public long getEventsCreated() {
            return eventsCreated;
        }

        public long getBookingsReceived() {
            return bookingsReceived;
        }
    }
}
