package com.rentalplatform.backend.vehicle.repository;

import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;


import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByLicensePlateAndDeletedFalse(String plateNumber);

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


    boolean existsByLicensePlateAndIdNotAndDeletedFalse(String licensePlate, UUID id);

    Optional<Vehicle> findByIdAndDeletedFalse(UUID vehicleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT v FROM Vehicle v WHERE v.id = :id AND v.deleted = false")
    Optional<Vehicle> findByIdWithLock(@Param("id") UUID id);
    
}
