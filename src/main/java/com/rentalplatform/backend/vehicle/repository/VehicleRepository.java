package com.rentalplatform.backend.vehicle.repository;

import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByLicensePlate(String plateNumber);

    Optional<Vehicle> findByIdAndVehicleOwnerIdAndDeletedFalse(
            UUID vehicleId,
            UUID ownerId
    );

    //Customer could browse all ACTIVE vehicle for rent
    Optional<Vehicle> findByIdAndStatusAndDeletedFalse(
            UUID id,
            VehicleStatus status
    );

    Page<Vehicle> findByStatusAndDeletedFalse(VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByVehicleOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);

    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);

}
