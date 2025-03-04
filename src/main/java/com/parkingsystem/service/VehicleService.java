package com.parkingsystem.service;

import com.parkingsystem.model.Vehicle;
import com.parkingsystem.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle registerEntry(String licensePlate, String vehicleType, String imagePath, String alprData) {
        // Check if vehicle is already parked
        Optional<Vehicle> existingVehicle = vehicleRepository.findByLicensePlateAndIsParkedTrue(licensePlate);
        if (existingVehicle.isPresent()) {
            throw new IllegalStateException("Vehicle is already parked");
        }

        // Create new vehicle entry
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setVehicleType(vehicleType);
        vehicle.setEntryTime(LocalDateTime.now());
        vehicle.setImagePath(imagePath);
        vehicle.setAlprData(alprData);
        vehicle.setIsParked(true);

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle registerExit(String licensePlate, Double parkingFee) {
        Vehicle vehicle = vehicleRepository.findByLicensePlateAndExitTimeIsNull(licensePlate)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found or already exited"));

        vehicle.setExitTime(LocalDateTime.now());
        vehicle.setParkingFee(parkingFee);
        vehicle.setIsParked(false);

        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getCurrentlyParkedVehicles() {
        return vehicleRepository.findByIsParkedTrueOrderByEntryTimeDesc();
    }

    public Optional<Vehicle> findParkedVehicle(String licensePlate) {
        return vehicleRepository.findByLicensePlateAndIsParkedTrue(licensePlate);
    }
}