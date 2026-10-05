package com.luxestay.hotel.controller;

import com.luxestay.hotel.entity.CheckInRecord;
import com.luxestay.hotel.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @GetMapping
    public ResponseEntity<List<CheckInRecord>> getAllRecords() {
        return ResponseEntity.ok(checkInService.getAllRecords());
    }

    @PostMapping
    public ResponseEntity<?> createRecord(@RequestBody Map<String, Object> body) {
        return arrive(body);
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody Map<String, Object> body) {
        return arrive(body);
    }

    @PostMapping("/arrive")
    public ResponseEntity<?> arrive(@RequestBody Map<String, Object> body) {
        Object bookingIdObj = body.get("bookingId");
        if (bookingIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "bookingId is required"));
        }
        Long bookingId = Long.valueOf(String.valueOf(bookingIdObj));
        String idType = (String) body.get("idType");
        String idNumber = (String) body.get("idNumber");
        String keyCardNumber = (String) body.get("keyCardNumber");

        try {
            CheckInRecord record = checkInService.checkIn(bookingId, idType, idNumber, keyCardNumber);
            return ResponseEntity.status(201).body(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/depart")
    public ResponseEntity<?> depart(@RequestBody Map<String, Object> body) {
        Object bookingIdObj = body.get("bookingId");
        if (bookingIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "bookingId is required"));
        }
        Long bookingId = Long.valueOf(String.valueOf(bookingIdObj));
        return checkInService.checkOut(bookingId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Active check-in record not found for booking")));
    }
}
