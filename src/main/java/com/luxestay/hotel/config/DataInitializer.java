package com.luxestay.hotel.config;

import com.luxestay.hotel.entity.CheckInRecord;
import com.luxestay.hotel.entity.Room;
import com.luxestay.hotel.entity.Staff;
import com.luxestay.hotel.entity.User;
import com.luxestay.hotel.repository.BookingRepository;
import com.luxestay.hotel.repository.CheckInRecordRepository;
import com.luxestay.hotel.repository.RoomRepository;
import com.luxestay.hotel.repository.StaffRepository;
import com.luxestay.hotel.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoomRepository roomRepository,
            StaffRepository staffRepository,
            CheckInRecordRepository checkInRecordRepository,
            BookingRepository bookingRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Check existing users and encrypt any plain text passwords
            userRepository.findAll().forEach(u -> {
                String p = u.getPassword();
                if (p != null && !p.startsWith("$2a$") && !p.startsWith("$2b$") && !p.startsWith("$2y$")) {
                    u.setPassword(passwordEncoder.encode(p));
                    userRepository.save(u);
                }
            });

            // Seed Users
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123"));
                admin.setName("Alexander Vance");
                admin.setEmail("admin@luxestay.com");
                admin.setPhone("+1 (555) 019-2831");
                admin.setRole("admin");
                admin.setDepartment("Executive Management");
                admin.setShift("Morning");
                admin.setEmployeeId("EMP-001");
                userRepository.save(admin);

                User staff = new User();
                staff.setUsername("staff");
                staff.setPassword(passwordEncoder.encode("123"));
                staff.setName("Elena Rostova");
                staff.setEmail("staff@luxestay.com");
                staff.setPhone("+1 (555) 019-8822");
                staff.setRole("receptionist");
                staff.setDepartment("Front Desk Operations");
                staff.setShift("Morning");
                staff.setEmployeeId("EMP-002");
                userRepository.save(staff);

                User guest = new User();
                guest.setUsername("new_guest");
                guest.setPassword(passwordEncoder.encode("123"));
                guest.setName("Julian Sterling");
                guest.setEmail("guest@luxestay.com");
                guest.setPhone("+1 (555) 321-9988");
                guest.setRole("guest");
                userRepository.save(guest);
            }

            // Seed Staff Roster
            if (staffRepository.count() == 0) {
                Staff s1 = new Staff();
                s1.setName("Elena Rostova");
                s1.setEmail("staff@luxestay.com");
                s1.setPhone("+1 (555) 019-8822");
                s1.setRole("Front Desk Supervisor");
                s1.setDepartment("Front Desk");
                s1.setShift("Morning");
                s1.setSalary(48000.0);
                s1.setStatus("Active");
                staffRepository.save(s1);

                Staff s2 = new Staff();
                s2.setName("Marcus Sterling");
                s2.setEmail("marcus@luxestay.com");
                s2.setPhone("+1 (555) 234-5678");
                s2.setRole("Head Concierge");
                s2.setDepartment("Concierge");
                s2.setShift("Afternoon");
                s2.setSalary(45000.0);
                s2.setStatus("Active");
                staffRepository.save(s2);

                Staff s3 = new Staff();
                s3.setName("Sarah Jenkins");
                s3.setEmail("sarah@luxestay.com");
                s3.setPhone("+1 (555) 345-6789");
                s3.setRole("Housekeeping Lead");
                s3.setDepartment("Housekeeping");
                s3.setShift("Morning");
                s3.setSalary(42000.0);
                s3.setStatus("Active");
                staffRepository.save(s3);
            }

            // Seed Rooms
            if (roomRepository.count() == 0) {
                Room r101 = new Room();
                r101.setRoomNumber("101");
                r101.setName("Deluxe King Suite");
                r101.setType("deluxe");
                r101.setPricePerNight(18500.0);
                r101.setCapacity(2);
                r101.setStatus("available");
                r101.setHousekeepingStatus("clean");
                r101.setDescription("Luxurious ocean-facing king suite with marble bathroom and private balcony.");
                r101.setAmenities("King Bed, Ocean View, Wi-Fi 6, Espresso Bar, Marble Jacuzzi");
                r101.setImageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80");
                roomRepository.save(r101);

                Room r102 = new Room();
                r102.setRoomNumber("102");
                r102.setName("Executive Skyline Room");
                r102.setType("executive");
                r102.setPricePerNight(24000.0);
                r102.setCapacity(3);
                r102.setStatus("available");
                r102.setHousekeepingStatus("clean");
                r102.setDescription("Panoramic city skyline views, ergonomic workstation, and soaking tub.");
                r102.setAmenities("King Bed, Skyline Panorama, Deep Soaking Tub, Smart Workstation");
                r102.setImageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80");
                roomRepository.save(r102);

                Room r103 = new Room();
                r103.setRoomNumber("103");
                r103.setName("Presidential Penthouse");
                r103.setType("suite");
                r103.setPricePerNight(45000.0);
                r103.setCapacity(4);
                r103.setStatus("available");
                r103.setHousekeepingStatus("clean");
                r103.setDescription("Top-tier penthouse with private plunge pool, butler service, and grand dining room.");
                r103.setAmenities("Butler Service, Private Plunge Pool, Grand Piano, Wine Cellar");
                r103.setImageUrl("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=1200&q=80");
                roomRepository.save(r103);
            }

            // Bidirectional Auto-Sync on startup
            try {
                // 1. Sync every Staff record into Users
                staffRepository.findAll().forEach(st -> {
                    if (st.getEmail() != null && !st.getEmail().trim().isEmpty()) {
                        String email = st.getEmail().trim().toLowerCase();
                        if (userRepository.findByEmail(email).isEmpty()) {
                            User u = new User();
                            String prefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
                            if (prefix.length() < 3) prefix = "staff_" + prefix;
                            String candidate = prefix;
                            int cnt = 1;
                            while (userRepository.existsByUsername(candidate)) {
                                candidate = prefix + "_" + cnt++;
                            }
                            u.setUsername(candidate);
                            u.setName(st.getName());
                            u.setEmail(email);
                            u.setPhone(st.getPhone() != null ? st.getPhone() : "+1 (555) 019-0000");
                            u.setPassword(passwordEncoder.encode("123"));
                            u.setRole(st.getRole() != null && st.getRole().toLowerCase().contains("reception") ? "receptionist" : "staff");
                            u.setDepartment(st.getDepartment());
                            u.setShift(st.getShift());
                            u.setEmployeeId("EMP-" + st.getId());
                            userRepository.save(u);
                        }
                    }
                });

                // 2. Sync every User with staff-like role into Staff roster
                userRepository.findAll().forEach(u -> {
                    String r = u.getRole() != null ? u.getRole().toLowerCase() : "";
                    if (r.contains("staff") || r.contains("reception") || r.contains("concierge") || r.contains("housekeeping")) {
                        if (u.getEmail() != null && !u.getEmail().trim().isEmpty()) {
                            String email = u.getEmail().trim().toLowerCase();
                            if (staffRepository.findByEmailIgnoreCase(email).isEmpty()) {
                                Staff st = new Staff();
                                st.setName(u.getName() != null && !u.getName().trim().isEmpty() ? u.getName() : u.getUsername());
                                st.setEmail(email);
                                st.setPhone(u.getPhone() != null && !u.getPhone().trim().isEmpty() ? u.getPhone() : "+1 (555) 019-0000");
                                st.setRole(r.contains("reception") ? "Front Desk Receptionist" : "Staff Member");
                                st.setDepartment(u.getDepartment() != null && !u.getDepartment().trim().isEmpty() ? u.getDepartment() :
                                        (r.contains("reception") ? "Front Desk" : "Operations"));
                                st.setShift(u.getShift() != null && !u.getShift().trim().isEmpty() ? u.getShift() : "Morning");
                                st.setSalary(45000.0);
                                st.setStatus("Active");
                                staffRepository.save(st);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Startup auto-sync warning: " + e.getMessage());
            }
        };
    }
}
