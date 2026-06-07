package com.rentalplatform.backend.vehicle.dto.request;

import com.rentalplatform.backend.vehicle.enums.FuelType;
import com.rentalplatform.backend.vehicle.enums.TransmissionType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateVehicleRequest {
    @NotBlank
    @Size(max = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    private String model;

    @NotNull
    @Min(1990)
    private Integer year;

    @NotBlank
    @Size(max = 20)
    private String licensePlate;

    private Integer seatCount;

    private FuelType fuelType;

    private TransmissionType transmission;

    @NotNull
    @PositiveOrZero
    private Integer mileage;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Positive
    private BigDecimal pricePerDay;

    @NotNull
    @Positive
    private BigDecimal depositAmount;
}
