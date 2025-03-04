package com.parking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String licensePlate;

    private LocalDateTime entryTime;
    
    private LocalDateTime exitTime;
    
    private String vehicleType;
    
    private String parkingSpot;
    
    private String status; // PARKED, EXITED
    
    private String imagePath; // Path to the captured license plate image
}