package com.luxestay.hotel.repository;

import com.luxestay.hotel.entity.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    Optional<CheckInRecord> findByBookingId(Long bookingId);
    List<CheckInRecord> findByStatus(String status);
}
