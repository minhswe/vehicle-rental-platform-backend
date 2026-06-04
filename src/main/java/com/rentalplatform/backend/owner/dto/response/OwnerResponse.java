package com.rentalplatform.backend.owner.dto.response;

import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerResponse {
    private UUID id;

    private UUID userId;

    private String businessName;

    private String description;

    private String verifiedStatus;

    private BigDecimal ratingAvg;

    private Integer totalVehicles;

    private Instant createdAt;
}

