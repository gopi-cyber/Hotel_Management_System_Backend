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

        String validIdNumber = (idNumber != null && !idNumber.trim().isEmpty())
                ? idNumber.trim()
                : "KYC-" + booking.getId();

        booking.setStatus("checked_in");
        bookingRepository.save(booking);

        if (booking.getRoomId() != null) {
            roomRepository.findById(booking.getRoomId()).ifPresent(r -> {
                r.setStatus("occupied");
                roomRepository.save(r);
            });
        }

        CheckInRecord record = checkInRecordRepository.findByBookingId(booking.getId())
                .orElseGet(CheckInRecord::new);
        record.setBookingId(booking.getId());
        record.setRoomId(booking.getRoomId() != null ? booking.getRoomId() : 1L);
        record.setGuestName(booking.getGuestName() != null ? booking.getGuestName() : "Guest");
        record.setIdType(idType != null ? idType : "Passport");
        record.setIdNumber(validIdNumber);
        record.setKeyCardNumber(keyCard != null ? keyCard : "KEY-" + (booking.getRoomNumber() != null ? booking.getRoomNumber() : "101"));
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus("active");
        record.setCheckOutTime(null);

        return checkInRecordRepository.save(record);
    }

    public Optional<CheckInRecord> checkOut(Long bookingId) {
        Optional<CheckInRecord> opt = checkInRecordRepository.findByBookingId(bookingId);
        CheckInRecord record;
        if (opt.isPresent()) {
            record = opt.get();
        } else {
            Booking b = bookingRepository.findById(bookingId).orElse(null);
            if (b == null) return Optional.empty();
            record = new CheckInRecord();
            record.setBookingId(b.getId());
            record.setRoomId(b.getRoomId() != null ? b.getRoomId() : 1L);
            record.setGuestName(b.getGuestName() != null ? b.getGuestName() : "Guest");
            record.setIdType("Passport");
            record.setIdNumber("KYC-" + b.getId());
            record.setKeyCardNumber("KEY-" + (b.getRoomNumber() != null ? b.getRoomNumber() : "101"));
            record.setCheckInTime(LocalDateTime.now().minusDays(1));
        }

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

        return Optional.of(checkInRecordRepository.save(record));
    }
}
