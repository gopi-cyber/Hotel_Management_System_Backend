package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.ServiceRequest;
import com.luxestay.hotel.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceTicketService {

    @Autowired
    private ServiceRequestRepository repository;

    public List<ServiceRequest> getAllServices(Long userId) {
        if (userId != null) {
            return repository.findByUserId(userId);
        }
        return repository.findAll();
    }

    public ServiceRequest createService(ServiceRequest request) {
        return repository.save(request);
    }

    public Optional<ServiceRequest> updateStatus(Long id, String status) {
        return repository.findById(id).map(s -> {
            s.setStatus(status);
            return repository.save(s);
        });
    }

    public Optional<ServiceRequest> updateService(Long id, ServiceRequest updates) {
        return repository.findById(id).map(s -> {
            if (updates.getStatus() != null) s.setStatus(updates.getStatus());
            if (updates.getPriority() != null) s.setPriority(updates.getPriority());
            if (updates.getAssignedStaff() != null) s.setAssignedStaff(updates.getAssignedStaff());
            if (updates.getDescription() != null) s.setDescription(updates.getDescription());
            return repository.save(s);
        });
    }
}
