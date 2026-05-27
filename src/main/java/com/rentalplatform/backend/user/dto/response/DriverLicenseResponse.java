package com.rentalplatform.backend.user.dto.response;

import com.rentalplatform.backend.user.enums.LicenseVerificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class DriverLicenseResponse {
    private UUID id;

    private String licenseNumber;

    private String frontImageUrl;

    private String backImageUrl;

    private LocalDate expiryDate;

    private LicenseVerificationStatus licenseVerificationStatus;

    private Instant verifiedAt;

    private UUID verifiedBy;

    private String rejectedReason;
}
