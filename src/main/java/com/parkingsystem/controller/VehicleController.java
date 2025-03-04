package com.parkingsystem.controller;

import com.parkingsystem.model.Vehicle;
import com.parkingsystem.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/entry")
    public ResponseEntity<Vehicle> registerEntry(@RequestBody Map<String, String> request) {
        Vehicle vehicle = vehicleService.registerEntry(
            request.get("licensePlate"),
            request.get("vehicleType"),
            request.get("imagePath"),
            request.get("alprData")
        );
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping("/exit/{licensePlate}")
    public ResponseEntity<Vehicle> registerExit(
            @PathVariable String licensePlate,
            @RequestBody Map<String, Double> request) {
        Vehicle vehicle = vehicleService.registerExit(licensePlate, request.get("parkingFee"));
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/parked")
    public ResponseEntity<List<Vehicle>> getCurrentlyParkedVehicles() {
        return ResponseEntity.ok(vehicleService.getCurrentlyParkedVehicles());
    }

    @GetMapping("/check/{licensePlate}")
    public ResponseEntity<?> checkVehicle(@PathVariable String licensePlate) {
        return vehicleService.findParkedVehicle(licensePlate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}