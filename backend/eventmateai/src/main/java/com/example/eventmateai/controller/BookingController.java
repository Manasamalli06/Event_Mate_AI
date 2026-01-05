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

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // ADMIN: get all bookings
    // GET http://localhost:9098/api/bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
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
        if(req.getSeats() != null && !req.getSeats().isEmpty()){
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
