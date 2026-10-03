package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.Booking;
import com.luxestay.hotel.entity.CheckInRecord;
import com.luxestay.hotel.entity.Room;
import com.luxestay.hotel.repository.BookingRepository;
import com.luxestay.hotel.repository.CheckInRecordRepository;
import com.luxestay.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInService {

    @Autowired
    private CheckInRecordRepository checkInRecordRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<CheckInRecord> getAllRecords() {
        return checkInRecordRepository.findAll();
    }

    public CheckInRecord checkIn(Long bookingId, String idType, String idNumber, String keyCard) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        booking.setStatus("checked_in");
        bookingRepository.save(booking);

        if (booking.getRoomId() != null) {
            roomRepository.findById(booking.getRoomId()).ifPresent(r -> {
                r.setStatus("occupied");
                roomRepository.save(r);
            });
        }

        CheckInRecord record = new CheckInRecord();
        record.setBookingId(booking.getId());
        record.setRoomId(booking.getRoomId());
        record.setGuestName(booking.getGuestName());
        record.setIdType(idType != null ? idType : "Passport");
        record.setIdNumber(idNumber != null ? idNumber : "ID-VERIFIED");
        record.setKeyCardNumber(keyCard != null ? keyCard : "KEY-" + booking.getRoomId());
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus("active");

        return checkInRecordRepository.save(record);
    }

    public Optional<CheckInRecord> checkOut(Long bookingId) {
        return checkInRecordRepository.findByBookingId(bookingId).map(record -> {
            record.setCheckOutTime(LocalDateTime.now());
            record.setStatus("checked_out");

            bookingRepository.findById(bookingId).ifPresent(b -> {
                b.setStatus("checked_out");
                bookingRepository.save(b);

                if (b.getRoomId() != null) {
                    roomRepository.findById(b.getRoomId()).ifPresent(r -> {
                        r.setStatus("available");
                        r.setHousekeepingStatus("dirty");
                        roomRepository.save(r);
                    });
                }
            });

            return checkInRecordRepository.save(record);
        });
    }
}
