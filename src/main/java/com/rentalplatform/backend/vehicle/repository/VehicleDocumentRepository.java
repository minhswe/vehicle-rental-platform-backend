package com.rentalplatform.backend.vehicle.repository;

import com.rentalplatform.backend.vehicle.entity.VehicleDocument;
import com.rentalplatform.backend.vehicle.enums.DocumentType;
import com.rentalplatform.backend.vehicle.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {

    List<VehicleDocument> findByVehicleIdAndDeletedFalse(UUID vehicleId);

    Optional<VehicleDocument>
    findByIdAndVehicleVehicleOwnerIdAndDeletedFalse(
            UUID documentId,
            UUID ownerId
    );

    long countByVehicleIdAndDeletedFalse(UUID vehicleId);

    boolean existsByVehicleIdAndDocumentTypeAndDeletedFalse(
            UUID vehicleId,
            DocumentType documentType
    );

    //=====================================
    // ADMIN
    //=====================================

    Optional<VehicleDocument> findByIdAndDeletedFalse(
            UUID id
    );

    Page<VehicleDocument>
    findByVerificationStatusAndDeletedFalse(
            VerificationStatus verificationStatus,
            Pageable pageable
    );
}
