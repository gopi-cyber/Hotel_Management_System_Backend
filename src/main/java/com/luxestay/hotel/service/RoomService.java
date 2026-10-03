package com.luxestay.hotel.service;

import com.luxestay.hotel.entity.Room;
import com.luxestay.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms(String status) {
        if (status != null && !status.isEmpty()) {
            return roomRepository.findByStatus(status);
        }
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Optional<Room> updateRoom(Long id, Room updates) {
        return roomRepository.findById(id).map(r -> {
            if (updates.getRoomNumber() != null) r.setRoomNumber(updates.getRoomNumber());
            if (updates.getName() != null) r.setName(updates.getName());
            if (updates.getType() != null) r.setType(updates.getType());
            if (updates.getPricePerNight() != null) r.setPricePerNight(updates.getPricePerNight());
            if (updates.getCapacity() != null) r.setCapacity(updates.getCapacity());
            if (updates.getStatus() != null) r.setStatus(updates.getStatus());
            if (updates.getHousekeepingStatus() != null) r.setHousekeepingStatus(updates.getHousekeepingStatus());
            if (updates.getDescription() != null) r.setDescription(updates.getDescription());
            if (updates.getAmenities() != null) r.setAmenities(updates.getAmenities());
            if (updates.getImageUrl() != null) r.setImageUrl(updates.getImageUrl());
            if (updates.getGalleryImages() != null) r.setGalleryImages(updates.getGalleryImages());
            return roomRepository.save(r);
        });
    }

    public boolean deleteRoom(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
