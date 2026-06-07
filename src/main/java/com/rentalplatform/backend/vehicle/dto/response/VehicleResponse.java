package com.rentalplatform.backend.vehicle.dto.response;

import com.rentalplatform.backend.vehicle.enums.FuelType;
import com.rentalplatform.backend.vehicle.enums.TransmissionType;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private UUID id;

    private UUID ownerId;

    private String brand;

    private String model;

    private Integer year;

    private String licensePlate;

    private Integer seatCount;

    private FuelType fuelType;

    private TransmissionType transmission;

    private Integer mileage;

    private String description;

    private BigDecimal pricePerDay;

    private BigDecimal depositAmount;

    private VehicleStatus status;
}
