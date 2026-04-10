package com.rentalplatform.backend.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class DriverLicenseResponse {
    private UUID id;
    private String licenseNumber;
    private String frontImageUrl;
    private String backImageUrl;
    private LocalDate expiryDate;
    private String verificationStatus;
}
