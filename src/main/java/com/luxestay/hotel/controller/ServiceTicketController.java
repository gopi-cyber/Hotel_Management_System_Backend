package com.luxestay.hotel.controller;

import com.luxestay.hotel.entity.ServiceRequest;
import com.luxestay.hotel.service.ServiceTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceTicketController {

    @Autowired
    private ServiceTicketService serviceTicketService;

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getAllServices(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(serviceTicketService.getAllServices(userId));
    }

    @PostMapping
    public ResponseEntity<ServiceRequest> createService(@RequestBody ServiceRequest request) {
        return ResponseEntity.status(201).body(serviceTicketService.createService(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }
        return serviceTicketService.updateStatus(id, status)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody ServiceRequest updates) {
        return serviceTicketService.updateService(id, updates)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
