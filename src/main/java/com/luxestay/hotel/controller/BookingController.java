package com.luxestay.hotel.controller;

import com.luxestay.hotel.entity.Booking;
import com.luxestay.hotel.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long guestId) {
        Long targetUser = userId != null ? userId : guestId;
        return ResponseEntity.ok(bookingService.getAllBookings(targetUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        return ResponseEntity.status(201).body(bookingService.createBooking(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Booking updates) {
        return bookingService.updateBooking(id, updates)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status field is required"));
        }
        return bookingService.updateStatus(id, status)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<?> updatePayment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String paymentStatus = body.get("paymentStatus");
        if (paymentStatus == null) {
            paymentStatus = body.get("status");
        }
        if (paymentStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Payment status is required"));
        }
        return bookingService.updatePayment(id, paymentStatus)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        if (bookingService.deleteBooking(id)) {
            return ResponseEntity.ok(Map.of("message", "Booking removed"));
        }
        return ResponseEntity.notFound().build();
    }
}
