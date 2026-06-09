package com.rentalplatform.backend.vehicle.dto.response;

import com.rentalplatform.backend.vehicle.enums.DocumentType;
import com.rentalplatform.backend.vehicle.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public class VehicleDocumentResponse {
    private UUID id;

    private DocumentType documentType;

    private String documentUrl;

    private VerificationStatus verificationStatus;

    private Instant createAt;

    private Instant verifiedAt;
}
