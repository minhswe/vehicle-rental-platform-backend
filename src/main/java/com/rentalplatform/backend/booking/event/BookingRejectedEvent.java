package com.rentalplatform.backend.booking.event;

import com.rentalplatform.backend.common.event.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString(callSuper = true)
public class BookingRejectedEvent extends DomainEvent {
    private final UUID bookingId;
    private final UUID customerId;
    private final UUID ownerId;

}
