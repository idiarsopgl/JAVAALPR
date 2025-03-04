package com.parking.repository;

import com.parking.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlateAndStatusNot(String licensePlate, String status);
    
    Optional<Vehicle> findByParkingSpotAndStatusNot(String parkingSpot, String status);
    
    boolean existsByParkingSpotAndStatusNot(String parkingSpot, String status);
}