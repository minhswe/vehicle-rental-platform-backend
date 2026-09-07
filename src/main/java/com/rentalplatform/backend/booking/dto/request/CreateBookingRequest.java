package com.rentalplatform.backend.booking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class CreateBookingRequest {

    @NotNull
    private UUID vehicleId;

    @NotNull
    @Future
    private Instant startTime;

    @NotNull
    @Future
    private Instant endTime;
}
