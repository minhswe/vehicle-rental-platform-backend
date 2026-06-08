package com.rentalplatform.backend.vehicle.entity;

import com.rentalplatform.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicle_images")
@Getter
@Setter
public class VehicleImage extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private String imageUrl;

    private Integer sortOrder;

}
