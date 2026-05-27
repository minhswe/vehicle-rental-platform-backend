package com.rentalplatform.backend.user.entity;

import com.rentalplatform.backend.user.enums.LicenseVerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "driver_licenses")
@Getter
@Setter
public class DriverLicense {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Column(name = "front_image_url", nullable = false, length = 1000)
    private String frontImageUrl;

    @Column(name = "back_image_url", nullable = false, length = 1000)
    private String backImageUrl;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private LicenseVerificationStatus verificationStatus = LicenseVerificationStatus.PENDING;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "rejected_reason", length = 1000)
    private String rejectedReason;
}
