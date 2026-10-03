package com.luxestay.hotel.controller;

import com.luxestay.hotel.dto.LoginRequest;
import com.luxestay.hotel.dto.RegisterRequest;
import com.luxestay.hotel.dto.UserResponse;
import com.luxestay.hotel.entity.User;
import com.luxestay.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return userService.authenticate(req)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid username or password")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            UserResponse res = userService.register(req);
            return ResponseEntity.status(201).body(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping
    public ResponseEntity<?> updateRole(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        Object roleObj = body.get("role");
        if (idObj == null || roleObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User ID and role are required"));
        }
        Long id = Long.valueOf(String.valueOf(idObj));
        String role = String.valueOf(roleObj);
        return userService.updateRole(id, role)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody User updates) {
        return userService.updateProfile(id, updates)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
