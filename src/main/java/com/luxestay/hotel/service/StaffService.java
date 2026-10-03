package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.Staff;
import com.luxestay.hotel.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    public List<Staff> getAllStaff(String department) {
        if (department != null && !department.isEmpty()) {
            return staffRepository.findByDepartment(department);
        }
        return staffRepository.findAll();
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
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
        return staffRepository.save(staff);
    }

    public Optional<Staff> updateStaff(Long id, Staff updates) {
        return staffRepository.findById(id).map(s -> {
            if (updates.getName() != null) s.setName(updates.getName());
            if (updates.getEmail() != null) s.setEmail(updates.getEmail());
            if (updates.getPhone() != null) s.setPhone(updates.getPhone());
            if (updates.getRole() != null) s.setRole(updates.getRole());
            if (updates.getDepartment() != null) s.setDepartment(updates.getDepartment());
            if (updates.getShift() != null) s.setShift(updates.getShift());
            if (updates.getSalary() != null) s.setSalary(updates.getSalary());
            if (updates.getStatus() != null) s.setStatus(updates.getStatus());
            return staffRepository.save(s);
        });
    }

    public boolean deleteStaff(Long id) {
        if (staffRepository.existsById(id)) {
            staffRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
