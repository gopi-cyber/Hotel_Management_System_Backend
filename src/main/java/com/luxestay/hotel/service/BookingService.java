package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.Booking;
import com.luxestay.hotel.entity.Room;
import com.luxestay.hotel.repository.BookingRepository;
import com.luxestay.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<Booking> getAllBookings(Long userId) {
        if (userId != null) {
            return bookingRepository.findByUserId(userId);
        }
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(Booking booking) {
        if (booking.getRoomNumber() == null && booking.getRoomId() != null) {
            roomRepository.findById(booking.getRoomId())
                    .ifPresent(r -> booking.setRoomNumber(r.getRoomNumber()));
        }
        return bookingRepository.save(booking);
    }

    public Optional<Booking> updateBooking(Long id, Booking updates) {
        return bookingRepository.findById(id).map(b -> {
            if (updates.getGuestName() != null) b.setGuestName(updates.getGuestName());
            if (updates.getGuestEmail() != null) b.setGuestEmail(updates.getGuestEmail());
            if (updates.getGuestPhone() != null) b.setGuestPhone(updates.getGuestPhone());
            if (updates.getCheckInDate() != null) b.setCheckInDate(updates.getCheckInDate());
            if (updates.getCheckOutDate() != null) b.setCheckOutDate(updates.getCheckOutDate());
            if (updates.getTotalPrice() != null) b.setTotalPrice(updates.getTotalPrice());
            if (updates.getPaymentStatus() != null) b.setPaymentStatus(updates.getPaymentStatus());
            if (updates.getStatus() != null) b.setStatus(updates.getStatus());
            if (updates.getSpecialRequests() != null) b.setSpecialRequests(updates.getSpecialRequests());
            if (updates.getIncidentalsTotal() != null) b.setIncidentalsTotal(updates.getIncidentalsTotal());
            return bookingRepository.save(b);
        });
    }

    public Optional<Booking> updateStatus(Long id, String status) {
        return bookingRepository.findById(id).map(b -> {
            b.setStatus(status);
            if ("checked_in".equalsIgnoreCase(status) && b.getRoomId() != null) {
                roomRepository.findById(b.getRoomId()).ifPresent(r -> {
                    r.setStatus("occupied");
                    roomRepository.save(r);
                });
            } else if ("checked_out".equalsIgnoreCase(status) && b.getRoomId() != null) {
                roomRepository.findById(b.getRoomId()).ifPresent(r -> {
                    r.setStatus("available");
                    r.setHousekeepingStatus("dirty");
                    roomRepository.save(r);
                });
            }
            return bookingRepository.save(b);
        });
    }

    public Optional<Booking> updatePayment(Long id, String paymentStatus) {
        return bookingRepository.findById(id).map(b -> {
            b.setPaymentStatus(paymentStatus);
            return bookingRepository.save(b);
        });
    }

    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
