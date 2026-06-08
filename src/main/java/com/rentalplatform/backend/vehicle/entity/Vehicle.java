package com.rentalplatform.backend.vehicle.entity;

import com.rentalplatform.backend.common.entity.BaseEntity;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.vehicle.enums.FuelType;
import com.rentalplatform.backend.vehicle.enums.TransmissionType;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
public class Vehicle extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vehicle_owner_id",
            nullable = false
    )
    private VehicleOwner vehicleOwner;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(
            unique = true,
            nullable = false
    )
    private String licensePlate;

    private Integer seatCount;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private TransmissionType transmission;

    @Column(nullable = false)
    private Integer mileage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal pricePerDay;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @Version
    private Long version; //prevent lost update
}
