package com.rentalplatform.backend.booking.event;

import com.rentalplatform.backend.common.event.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
@RequiredArgsConstructor
public class BookingCreatedEvent extends DomainEvent {

    private final UUID bookingId;
    private final UUID customerId;
    private final UUID ownerId;
    private final UUID vehicleId;
    private final BigDecimal totalAmount;
}


