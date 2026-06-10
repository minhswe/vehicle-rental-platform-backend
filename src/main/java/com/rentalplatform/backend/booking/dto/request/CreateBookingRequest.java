package com.rentalplatform.backend.booking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateBookingRequest {


    private UUID vehicleId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
