package com.rentalplatform.backend.vehicle.repository;

import com.rentalplatform.backend.vehicle.entity.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleImageRepository extends JpaRepository<VehicleImage, UUID> {
    List<VehicleImage> findByVehicleIdOrderBySortOrderAsc(
            UUID vehicleId
    );

    long countByVehicleId(UUID vehicleId);

    Optional<VehicleImage>
    findByIdAndVehicleVehicleOwnerId(
            UUID imageId,
            UUID ownerId
    );
}
