package com.luxestay.hotel.repository;

import com.luxestay.hotel.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByDepartment(String department);
    List<Staff> findByShift(String shift);
    List<Staff> findByStatus(String status);
}
