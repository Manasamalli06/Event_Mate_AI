package com.example.eventmateai.controller;

import java.util.List;
import java.util.Map;

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

import com.example.eventmateai.dto.BookingRequest;
import com.example.eventmateai.model.Booking;
import com.example.eventmateai.repository.BookingRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final com.example.eventmateai.repository.EventRepository eventRepository;
    private final com.example.eventmateai.repository.UserRepository userRepository;

    public BookingController(BookingRepository bookingRepository,
            com.example.eventmateai.repository.EventRepository eventRepository,
            com.example.eventmateai.repository.UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // ADMIN: get all bookings with details
    // GET http://localhost:9098/api/bookings
    @GetMapping
    public List<com.example.eventmateai.dto.BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(b -> {
            com.example.eventmateai.dto.BookingResponse resp = new com.example.eventmateai.dto.BookingResponse();
            resp.setId(b.getId());
            resp.setUserId(b.getUserId());
            resp.setEventId(b.getEventId());
            resp.setTickets(b.getTickets());
            resp.setPaymentMode(b.getPaymentMode());
            resp.setStatus(b.getStatus());
            resp.setCreatedAt(b.getCreatedAt());
            resp.setEventDateTime(b.getEventDateTime());
            resp.setSeats(b.getSeats());

            // Enrich with titles/names
            eventRepository.findById(b.getEventId()).ifPresent(e -> resp.setEventTitle(e.getTitle()));
            userRepository.findById(b.getUserId()).ifPresent(u -> resp.setUserName(u.getFullName())); // Assuming User
                                                                                                      // has
                                                                                                      // getFullName()

            return resp;
        }).collect(java.util.stream.Collectors.toList());
    }

    // USER: create a booking
    // POST http://localhost:9098/api/bookings
    @PostMapping
    public Booking createBooking(@RequestBody BookingRequest req) {
        Booking b = new Booking();
        b.setUserId(req.getUserId());
        b.setEventId(req.getEventId());
        b.setTickets(req.getTickets());
        b.setPaymentMode(req.getPaymentMode());
        b.setStatus(req.getStatus());
        b.setEventDateTime(req.getEventDateTime());
        if (req.getSeats() != null && !req.getSeats().isEmpty()) {
            String seatsJoined = String.join(",", req.getSeats());
            b.setSeats(seatsJoined);
        } else {
            b.setSeats(null);
        }
        return bookingRepository.save(b);
    }

    // USER: get bookings for one user (My Bookings)
    // GET http://localhost:9098/api/bookings/user/{userId}
    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // ADMIN: update status (Confirm / Cancel)
    // PUT http://localhost:9098/api/bookings/{id}/status
    @PutMapping("/{id}/status")
    public Booking updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    // DELETE booking
    // DELETE http://localhost:9098/api/bookings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
