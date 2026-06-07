package com.rentalplatform.backend.vehicle.dto.request;

import com.rentalplatform.backend.vehicle.enums.FuelType;
import com.rentalplatform.backend.vehicle.enums.TransmissionType;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateVehicleRequest {
    private String brand;

    private String model;

    private Integer year;

    private Integer seatCount;

    private FuelType fuelType;

    private TransmissionType transmission;

    private Integer mileage;

    private String description;

    private BigDecimal pricePerDay;

    private BigDecimal depositAmount;

    private String licensePlate;

}
