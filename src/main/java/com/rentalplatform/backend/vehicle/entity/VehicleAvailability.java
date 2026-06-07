package com.rentalplatform.backend.vehicle.entity;

import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicle_availability")
@Getter
@Setter
public class VehicleAvailability {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;
}
