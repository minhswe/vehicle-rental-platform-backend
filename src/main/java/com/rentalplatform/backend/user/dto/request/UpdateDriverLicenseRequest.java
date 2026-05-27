package com.rentalplatform.backend.user.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UpdateDriverLicenseRequest {
    @Pattern(
            regexp = "^[A-Z0-9-]{6,20}$",
            message = "Invalid driver license number"
    )
    private String licenseNumber;

    @Future(message = "Expiry date must be in the future")
    private Instant expiryDate;
}
