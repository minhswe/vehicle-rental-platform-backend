package com.rentalplatform.backend.vehicle.entity;

import com.rentalplatform.backend.common.entity.BaseEntity;
import com.rentalplatform.backend.vehicle.enums.DocumentType;
import com.rentalplatform.backend.vehicle.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicle_documents")
@Getter
@Setter
public class VehicleDocument extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;


    private Instant verifiedAt;
}
