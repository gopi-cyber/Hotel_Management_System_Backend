package com.luxestay.hotel.service;

import com.luxestay.hotel.dto.LoginRequest;
import com.luxestay.hotel.dto.RegisterRequest;
import com.luxestay.hotel.dto.UserResponse;
import com.luxestay.hotel.entity.User;
import com.luxestay.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getName(), u.getEmail(),
                u.getPhone(), u.getRole(), u.getDepartment(), u.getShift(),
                u.getEmployeeId(), u.getAvatarUrl()
        );
    }

    public Optional<UserResponse> authenticate(LoginRequest req) {
        return userRepository.findByUsername(req.getUsername())
                .filter(u -> {
                    String stored = u.getPassword();
                    if (stored == null) return false;
                    // Support BCrypt hash or plain-text fallback during migration
                    if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                        return passwordEncoder.matches(req.getPassword(), stored);
                    }
                    return stored.equals(req.getPassword());
                })
                .map(this::toResponse);
    }

    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setRole(req.getRole() != null ? req.getRole().toLowerCase() : "guest");

        User saved = userRepository.save(u);
        return toResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Optional<UserResponse> updateRole(Long id, String role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role.toLowerCase());
            return toResponse(userRepository.save(user));
        });
    }

    public Optional<UserResponse> updateProfile(Long id, User updates) {
        return userRepository.findById(id).map(u -> {
            if (updates.getName() != null) u.setName(updates.getName());
            if (updates.getEmail() != null) u.setEmail(updates.getEmail());
            if (updates.getPhone() != null) u.setPhone(updates.getPhone());
            if (updates.getAvatarUrl() != null) u.setAvatarUrl(updates.getAvatarUrl());
            if (updates.getDepartment() != null) u.setDepartment(updates.getDepartment());
            if (updates.getShift() != null) u.setShift(updates.getShift());
            if (updates.getEmployeeId() != null) u.setEmployeeId(updates.getEmployeeId());
            return toResponse(userRepository.save(u));
        });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
