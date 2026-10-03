package com.luxestay.hotel.controller;

import com.luxestay.hotel.entity.Staff;
import com.luxestay.hotel.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff(@RequestParam(required = false) String department) {
        return ResponseEntity.ok(staffService.getAllStaff(department));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable Long id) {
        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Staff> createStaff(@RequestBody Staff staff) {
        return ResponseEntity.status(201).body(staffService.createStaff(staff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody Staff updates) {
        return staffService.updateStaff(id, updates)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        if (staffService.deleteStaff(id)) {
            return ResponseEntity.ok(Map.of("message", "Staff record deleted"));
        }
        return ResponseEntity.notFound().build();
    }
}
