package com.luxestay.hotel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_records")
public class CheckInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "id_type")
    private String idType; // Passport, National ID, Driving License

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "key_card_number")
    private String keyCardNumber;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime = LocalDateTime.now();

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    private String status = "active"; // active, checked_out

    public CheckInRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getKeyCardNumber() { return keyCardNumber; }
    public void setKeyCardNumber(String keyCardNumber) { this.keyCardNumber = keyCardNumber; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
