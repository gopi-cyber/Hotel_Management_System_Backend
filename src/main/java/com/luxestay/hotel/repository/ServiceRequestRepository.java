package com.luxestay.hotel.repository;

import com.luxestay.hotel.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByUserId(Long userId);
    List<ServiceRequest> findByStatus(String status);
    List<ServiceRequest> findByRoomNumber(String roomNumber);
}
