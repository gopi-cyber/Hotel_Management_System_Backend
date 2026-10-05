package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.Booking;
import com.luxestay.hotel.entity.Room;
import com.luxestay.hotel.repository.BookingRepository;
import com.luxestay.hotel.repository.CheckInRecordRepository;
import com.luxestay.hotel.repository.RoomRepository;
import com.luxestay.hotel.repository.StaffRepository;
import com.luxestay.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckInRecordRepository checkInRecordRepository;

    public Map<String, Object> getAnalytics() {
        List<Booking> bookings = bookingRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        long totalStaff = staffRepository.count();
        long totalUsers = userRepository.count();
        long totalCheckIns = checkInRecordRepository.count();

        double totalRevenue = bookings.stream()
                .filter(b -> !"cancelled".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0.0)
                .sum();

        long occupiedRooms = rooms.stream()
                .filter(r -> "occupied".equalsIgnoreCase(r.getStatus()))
                .count();

        long totalRooms = rooms.size();
        double occupancyRate = totalRooms > 0 ? ((double) occupiedRooms / (double) totalRooms) * 100.0 : 0.0;
        long totalBookings = bookings.size();
        double adr = totalBookings > 0 ? totalRevenue / totalBookings : 0.0;
        double revPar = totalRooms > 0 ? totalRevenue / (totalRooms > 0 ? totalRooms : 1) : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalBookings", totalBookings);
        result.put("totalRooms", totalRooms);
        result.put("occupiedRooms", occupiedRooms);
        result.put("availableRooms", Math.max(0, totalRooms - occupiedRooms));
        result.put("occupancyRate", Math.round(occupancyRate * 10.0) / 10.0);
        result.put("adr", Math.round(adr));
        result.put("revPar", Math.round(revPar));
        result.put("totalStaff", totalStaff);
        result.put("totalUsers", totalUsers);
        result.put("totalCheckIns", totalCheckIns);

        return result;
    }
}
