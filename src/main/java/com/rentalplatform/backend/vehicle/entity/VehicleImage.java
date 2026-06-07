package com.rentalplatform.backend.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.UUID;

@Entity
@Table(name = "vehicle_images")
@Getter
@Setter
public class VehicleImage {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private String imageUrl;

    private Integer sortOrder;
}
