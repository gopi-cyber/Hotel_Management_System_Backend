package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.Staff;
import com.luxestay.hotel.entity.User;
import com.luxestay.hotel.repository.StaffRepository;
import com.luxestay.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Staff> getAllStaff(String department) {
        if (department != null && !department.isEmpty()) {
            return staffRepository.findByDepartment(department);
        }
        return staffRepository.findAll();
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
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

    public Staff createStaff(Staff staff) {
        if (staff.getName() == null || staff.getName().trim().isEmpty()) {
            throw new RuntimeException("Staff name is required");
        }
        if (staff.getEmail() != null && !staff.getEmail().trim().isEmpty() && !staff.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Valid staff email is required");
        }
        if (staff.getPhone() != null && !staff.getPhone().trim().isEmpty()) {
            String cleanPhone = staff.getPhone().replaceAll("\\D", "");
            if (cleanPhone.length() < 10) {
                throw new RuntimeException("Valid 10-digit staff phone number is required");
            }
            staff.setPhone(cleanPhone.length() >= 10 ? cleanPhone.substring(cleanPhone.length() - 10) : cleanPhone);
        }
        Staff saved = staffRepository.save(staff);

        // Auto-sync into Users table
        try {
            if (saved.getEmail() != null && !saved.getEmail().trim().isEmpty()) {
                String email = saved.getEmail().trim().toLowerCase();
                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    User u = existingUser.get();
                    if (saved.getName() != null) u.setName(saved.getName());
                    if (saved.getPhone() != null) u.setPhone(saved.getPhone());
                    if (saved.getDepartment() != null) u.setDepartment(saved.getDepartment());
                    if (saved.getShift() != null) u.setShift(saved.getShift());
                    u.setEmployeeId("EMP-" + saved.getId());
                    if ("guest".equalsIgnoreCase(u.getRole())) {
                        u.setRole(saved.getRole() != null && saved.getRole().toLowerCase().contains("reception") ? "receptionist" : "staff");
                    }
                    userRepository.save(u);
                } else {
                    User u = new User();
                    String prefix = email.contains("@") ? email.split("@")[0] : saved.getName();
                    u.setUsername(generateUniqueUsername(prefix));
                    u.setName(saved.getName());
                    u.setEmail(email);
                    u.setPhone(saved.getPhone() != null && !saved.getPhone().isEmpty() ? saved.getPhone() : "+1 (555) 019-0000");
                    u.setPassword(passwordEncoder.encode("Password@123"));
                    u.setRole(saved.getRole() != null && saved.getRole().toLowerCase().contains("reception") ? "receptionist" : "staff");
                    u.setDepartment(saved.getDepartment());
                    u.setShift(saved.getShift());
                    u.setEmployeeId("EMP-" + saved.getId());
                    userRepository.save(u);
                }
            }
        } catch (Exception e) {
            System.err.println("Auto-sync staff to user error: " + e.getMessage());
        }

        return saved;
    }

    public Optional<Staff> updateStaff(Long id, Staff updates) {
        return staffRepository.findById(id).map(s -> {
            String oldEmail = s.getEmail();
            if (updates.getName() != null) s.setName(updates.getName());
            if (updates.getEmail() != null) s.setEmail(updates.getEmail());
            if (updates.getPhone() != null) s.setPhone(updates.getPhone());
            if (updates.getRole() != null) s.setRole(updates.getRole());
            if (updates.getDepartment() != null) s.setDepartment(updates.getDepartment());
            if (updates.getShift() != null) s.setShift(updates.getShift());
            if (updates.getSalary() != null) s.setSalary(updates.getSalary());
            if (updates.getStatus() != null) s.setStatus(updates.getStatus());
            Staff saved = staffRepository.save(s);

            // Auto-sync into Users table
            try {
                String targetEmail = saved.getEmail() != null ? saved.getEmail().trim().toLowerCase() : (oldEmail != null ? oldEmail.trim().toLowerCase() : null);
                if (targetEmail != null) {
                    Optional<User> uOpt = userRepository.findByEmail(targetEmail);
                    if (uOpt.isPresent()) {
                        User u = uOpt.get();
                        if (saved.getName() != null) u.setName(saved.getName());
                        if (saved.getEmail() != null) u.setEmail(saved.getEmail().trim().toLowerCase());
                        if (saved.getPhone() != null) u.setPhone(saved.getPhone());
                        if (saved.getDepartment() != null) u.setDepartment(saved.getDepartment());
                        if (saved.getShift() != null) u.setShift(saved.getShift());
                        if (!"admin".equalsIgnoreCase(u.getRole())) {
                            u.setRole(saved.getRole() != null && saved.getRole().toLowerCase().contains("reception") ? "receptionist" : "staff");
                        }
                        userRepository.save(u);
                    }
                }
            } catch (Exception e) {
                System.err.println("Auto-sync staff update to user error: " + e.getMessage());
            }

            return saved;
        });
    }

    public boolean deleteStaff(Long id) {
        Optional<Staff> opt = staffRepository.findById(id);
        if (opt.isPresent()) {
            Staff s = opt.get();
            String email = s.getEmail();
            staffRepository.deleteById(id);

            // Auto-sync into Users table: demote user to guest
            if (email != null && !email.trim().isEmpty()) {
                try {
                    userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(u -> {
                        if (!"admin".equalsIgnoreCase(u.getUsername()) && u.getId() != 1L) {
                            u.setRole("guest");
                            userRepository.save(u);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Auto-sync staff delete error: " + e.getMessage());
                }
            }

            return true;
        }
        return false;
    }
}
