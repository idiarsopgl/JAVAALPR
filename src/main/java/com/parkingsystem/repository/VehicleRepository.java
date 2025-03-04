package com.parkingsystem.repository;

import com.parkingsystem.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlateAndIsParkedTrue(String licensePlate);
    List<Vehicle> findByIsParkedTrue();
    List<Vehicle> findByIsParkedTrueOrderByEntryTimeDesc();
    Optional<Vehicle> findByLicensePlateAndExitTimeIsNull(String licensePlate);
}