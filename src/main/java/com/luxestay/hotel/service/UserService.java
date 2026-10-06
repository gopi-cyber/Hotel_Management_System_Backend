package com.luxestay.hotel.service;

import com.luxestay.hotel.dto.LoginRequest;
import com.luxestay.hotel.dto.RegisterRequest;
import com.luxestay.hotel.dto.UserResponse;
import com.luxestay.hotel.entity.User;
import com.luxestay.hotel.entity.Staff;
import com.luxestay.hotel.entity.Booking;
import com.luxestay.hotel.entity.ServiceRequest;
import com.luxestay.hotel.repository.UserRepository;
import com.luxestay.hotel.repository.StaffRepository;
import com.luxestay.hotel.repository.BookingRepository;
import com.luxestay.hotel.repository.ServiceRequestRepository;
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

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private StaffRepository staffRepository;

    @jakarta.annotation.PostConstruct
    public void syncStaffAndUsersOnStartup() {
        try {
            // 1. Sync any existing staff into users table if missing
            List<Staff> allStaff = staffRepository.findAll();
            for (Staff s : allStaff) {
                if (s.getEmail() != null && !s.getEmail().trim().isEmpty()) {
                    String email = s.getEmail().trim().toLowerCase();
                    if (!userRepository.findByEmail(email).isPresent()) {
                        User u = new User();
                        String prefix = email.contains("@") ? email.split("@")[0] : s.getName();
                        u.setUsername(generateUniqueUsername(prefix));
                        u.setName(s.getName() != null ? s.getName() : "Staff Member");
                        u.setEmail(email);
                        u.setPhone(s.getPhone() != null ? s.getPhone() : "+1 (555) 019-0000");
                        u.setPassword(passwordEncoder.encode("Password@123"));
                        u.setRole(s.getRole() != null && s.getRole().toLowerCase().contains("reception") ? "receptionist" : "staff");
                        u.setDepartment(s.getDepartment());
                        u.setShift(s.getShift());
                        u.setEmployeeId("EMP-" + s.getId());
                        userRepository.save(u);
                        System.out.println("Startup synced staff -> user: " + u.getUsername() + " (" + email + ")");
                    }
                }
            }

            // 2. Sync any users with staff/receptionist roles into staff table if missing
            List<User> staffUsers = userRepository.findAll().stream()
                    .filter(u -> isStaffRole(u.getRole()))
                    .collect(Collectors.toList());
            for (User u : staffUsers) {
                if (u.getEmail() != null && !u.getEmail().trim().isEmpty()) {
                    String email = u.getEmail().trim().toLowerCase();
                    if (!staffRepository.findByEmailIgnoreCase(email).isPresent()) {
                        syncUserToStaff(u);
                        System.out.println("Startup synced user -> staff: " + u.getUsername() + " (" + email + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Startup staff/user sync error: " + e.getMessage());
        }
    }

    private String generateUniqueUsername(String base) {
        String clean = (base != null ? base : "staff").replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (clean.length() < 3) clean = "staff_" + clean;
        if (clean.length() > 20) clean = clean.substring(0, 20);
        String candidate = clean;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = clean + "_" + suffix++;
        }
        return candidate;
    }

    public boolean isStaffRole(String role) {
        if (role == null) return false;
        String r = role.toLowerCase().trim();
        return r.contains("staff") || r.contains("reception") || r.contains("concierge") || r.contains("housekeeping") || r.contains("manager");
    }

    public void syncUserToStaff(User user) {
        if (user == null || user.getEmail() == null || !isStaffRole(user.getRole())) {
            return;
        }
        try {
            String email = user.getEmail().trim().toLowerCase();
            Optional<Staff> opt = staffRepository.findByEmailIgnoreCase(email);
            Staff staff = opt.orElseGet(Staff::new);
            staff.setName(user.getName() != null && !user.getName().trim().isEmpty() ? user.getName() : user.getUsername());
            staff.setEmail(email);
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                staff.setPhone(user.getPhone());
            } else if (staff.getPhone() == null) {
                staff.setPhone("+1 (555) 019-0000");
            }
            String roleTitle = "receptionist".equalsIgnoreCase(user.getRole()) ? "Front Desk Receptionist" :
                    ("staff".equalsIgnoreCase(user.getRole()) ? "Staff Member" : user.getRole());
            staff.setRole(roleTitle);
            staff.setDepartment(user.getDepartment() != null && !user.getDepartment().trim().isEmpty() ? user.getDepartment() :
                    ("receptionist".equalsIgnoreCase(user.getRole()) ? "Front Desk" : "Operations"));
            staff.setShift(user.getShift() != null && !user.getShift().trim().isEmpty() ? user.getShift() : "Morning");
            staff.setStatus("Active");
            if (staff.getSalary() == null || staff.getSalary() == 0.0) {
                staff.setSalary(45000.0);
            }
            staffRepository.save(staff);
        } catch (Exception e) {
            System.err.println("Failed to sync user to staff: " + e.getMessage());
        }
    }

    public UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getName(), u.getEmail(),
                u.getPhone(), u.getRole(), u.getDepartment(), u.getShift(),
                u.getEmployeeId(), u.getAvatarUrl()
        );
    }

    public Optional<UserResponse> authenticate(LoginRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return Optional.empty();
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(req.getUsername().trim())
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
        if (req.getUsername() == null || req.getUsername().trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        // Password complexity: 1 uppercase, 1 lowercase, 1 digit, 1 special character
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#^~()+\\-=\\[\\]{}|;:'\",.<>/?]).+$";
        if (!req.getPassword().matches(passwordRegex)) {
            throw new RuntimeException("Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character");
        }
        if (req.getEmail() == null || !req.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("A valid email address is required");
        }
        String cleanPhone = req.getPhone() != null ? req.getPhone().replaceAll("\\D", "") : "";
        if (cleanPhone.length() < 8) {
            throw new RuntimeException("Valid mobile phone number is required (at least 8 digits)");
        }
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new RuntimeException("Email already registered");
        }

        User u = new User();
        u.setUsername(req.getUsername().trim());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setName(req.getName() != null && !req.getName().trim().isEmpty() ? req.getName().trim() : req.getUsername().trim());
        u.setEmail(req.getEmail().trim().toLowerCase());
        u.setPhone(cleanPhone.length() >= 10 ? cleanPhone.substring(cleanPhone.length() - 10) : cleanPhone);
        u.setRole(req.getRole() != null ? req.getRole().toLowerCase() : "guest");

        User saved = userRepository.save(u);
        syncUserToStaff(saved);
        return toResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Optional<UserResponse> updateRole(Long id, String role) {
        return userRepository.findById(id).map(user -> {
            if (id == 1L || "admin".equalsIgnoreCase(user.getUsername())) {
                // Keep root admin as admin
                return toResponse(user);
            }
            String normalizedRole = role != null ? role.trim().toLowerCase() : "guest";
            user.setRole(normalizedRole);
            User saved = userRepository.save(user);

            // Auto-sync with Staff table
            if (isStaffRole(normalizedRole)) {
                syncUserToStaff(saved);
            } else {
                // Demoted to guest or non-staff: remove from staff roster
                if (saved.getEmail() != null) {
                    staffRepository.findByEmailIgnoreCase(saved.getEmail().trim().toLowerCase()).ifPresent(st -> {
                        staffRepository.deleteById(st.getId());
                    });
                }
            }

            return toResponse(saved);
        });
    }

    public Optional<UserResponse> updateProfile(Long id, User updates) {
        return userRepository.findById(id).map(u -> {
            if (updates.getName() != null) u.setName(updates.getName());
            if (updates.getEmail() != null) u.setEmail(updates.getEmail());
            if (updates.getPhone() != null) u.setPhone(updates.getPhone());
            if (updates.getPassword() != null && !updates.getPassword().trim().isEmpty()) {
                String current = updates.getCurrentPassword();
                if (current == null || current.trim().isEmpty()) {
                    throw new IllegalArgumentException("Current password is required to change password");
                }
                if (u.getPassword() == null || !passwordEncoder.matches(current.trim(), u.getPassword())) {
                    throw new SecurityException("Current password is incorrect");
                }
                u.setPassword(passwordEncoder.encode(updates.getPassword().trim()));
            }
            if (updates.getAvatarUrl() != null) u.setAvatarUrl(updates.getAvatarUrl());
            if (updates.getDepartment() != null) u.setDepartment(updates.getDepartment());
            if (updates.getShift() != null) u.setShift(updates.getShift());
            if (updates.getEmployeeId() != null) u.setEmployeeId(updates.getEmployeeId());
            User saved = userRepository.save(u);
            if (isStaffRole(saved.getRole())) {
                syncUserToStaff(saved);
            }
            return toResponse(saved);
        });
    }

    public boolean deleteUser(Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            User u = opt.get();
            if (id == 1L || "admin".equalsIgnoreCase(u.getUsername())) {
                return false; // Prevent deleting root admin
            }
            try {
                if (bookingRepository != null) {
                    List<Booking> bookings = bookingRepository.findByUserId(id);
                    if (bookings != null && !bookings.isEmpty()) {
                        bookingRepository.deleteAll(bookings);
                    }
                }
                if (serviceRequestRepository != null) {
                    List<ServiceRequest> reqs = serviceRequestRepository.findByUserId(id);
                    if (reqs != null && !reqs.isEmpty()) {
                        serviceRequestRepository.deleteAll(reqs);
                    }
                }
            } catch (Exception e) {
                // Ignore cascade errors
            }

            // Also remove from Staff roster if present
            if (u.getEmail() != null) {
                try {
                    staffRepository.findByEmailIgnoreCase(u.getEmail().trim().toLowerCase()).ifPresent(st -> {
                        staffRepository.deleteById(st.getId());
                    });
                } catch (Exception e) {
                    // Ignore staff delete error
                }
            }

            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
