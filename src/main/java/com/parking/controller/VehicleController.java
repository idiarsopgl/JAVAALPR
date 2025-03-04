package com.parking.controller;

import com.parking.model.Vehicle;
import com.parking.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/entry")
    public ResponseEntity<Vehicle> registerEntry(
            @RequestParam("licensePlateImage") MultipartFile licensePlateImage,
            @RequestParam("vehicleType") String vehicleType,
            @RequestParam("parkingSpot") String parkingSpot) {
        Vehicle vehicle = vehicleService.registerEntry(licensePlateImage, vehicleType, parkingSpot);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping("/exit/{parkingSpot}")
    public ResponseEntity<Vehicle> registerExit(@PathVariable String parkingSpot) {
        Vehicle vehicle = vehicleService.registerExit(parkingSpot);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllParkedVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllParkedVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}