package com.parking.service;

import com.parking.model.Vehicle;
import com.parking.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final ALPRService alprService;

    public VehicleService(VehicleRepository vehicleRepository, ALPRService alprService) {
        this.vehicleRepository = vehicleRepository;
        this.alprService = alprService;
    }

    @Transactional
    public Vehicle registerEntry(MultipartFile licensePlateImage, String vehicleType, String parkingSpot) {
        try {
            // Process the license plate image using ALPR
            String licensePlate = alprService.processImage(licensePlateImage);

            // Check if vehicle is already parked
            Optional<Vehicle> existingVehicle = vehicleRepository.findByLicensePlateAndStatusNot(
                licensePlate, "EXITED"
            );
            if (existingVehicle.isPresent()) {
                throw new IllegalStateException("Vehicle is already parked");
            }

            // Check if parking spot is occupied
            if (vehicleRepository.existsByParkingSpotAndStatusNot(parkingSpot, "EXITED")) {
                throw new IllegalStateException("Parking spot is already occupied");
            }

            // Create new vehicle entry
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate(licensePlate);
            vehicle.setVehicleType(vehicleType);
            vehicle.setParkingSpot(parkingSpot);
            vehicle.setEntryTime(LocalDateTime.now());
            vehicle.setStatus("PARKED");
            vehicle.setImagePath(alprService.getImagePath(licensePlateImage.getOriginalFilename()));

            return vehicleRepository.save(vehicle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process vehicle entry", e);
        }
    }

    @Transactional
    public Vehicle registerExit(String parkingSpot) {
        Vehicle vehicle = vehicleRepository.findByParkingSpotAndStatusNot(parkingSpot, "EXITED")
            .orElseThrow(() -> new IllegalStateException("No vehicle found in parking spot"));

        vehicle.setExitTime(LocalDateTime.now());
        vehicle.setStatus("EXITED");

        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllParkedVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }
}