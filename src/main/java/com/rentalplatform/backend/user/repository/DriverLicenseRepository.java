package com.rentalplatform.backend.user.repository;

import com.rentalplatform.backend.user.entity.DriverLicense;
import com.rentalplatform.backend.user.constant.LicenseVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverLicenseRepository extends JpaRepository<DriverLicense, UUID> {

    /**
     * Get driver license by user id
     */
    Optional<DriverLicense> findByUserId(UUID userId);

    /**
     * Check duplicated license number
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Find all licenses by verification status
     */
    List<DriverLicense> findByLicenseVerificationStatus(
            LicenseVerificationStatus licenseVerificationStatus
    );

    /**
     * Find expired licenses
     */
    List<DriverLicense> findByExpiryDateBefore(
            Instant date
    );

    /**
     * Check if user already uploaded license
     */
    boolean existsByUserId(UUID userId);
}
