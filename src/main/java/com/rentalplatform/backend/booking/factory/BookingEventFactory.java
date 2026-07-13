package com.rentalplatform.backend.booking.factory;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.event.BookingCancelledEvent;
import com.rentalplatform.backend.booking.event.BookingConfirmedEvent;
import com.rentalplatform.backend.booking.event.BookingCreatedEvent;
import com.rentalplatform.backend.booking.event.BookingRejectedEvent;
import org.springframework.stereotype.Component;

@Component
public class BookingEventFactory {
    public BookingCreatedEvent created(Booking booking) {
        return new BookingCreatedEvent(
                booking.getId(),
                booking.getCustomer()
                       .getId(),
                booking.getOwner()
                       .getId(),
                booking.getVehicle()
                       .getId(),
                booking.getTotalAmount()
        );
    }

    public BookingConfirmedEvent confirmed(Booking booking) {

        return new BookingConfirmedEvent(
                booking.getId(),
                booking.getCustomer()
                       .getId(),
                booking.getOwner()
                       .getId()
        );
    }

    public BookingRejectedEvent rejected(Booking booking) {

        return new BookingRejectedEvent(
                booking.getId(),
                booking.getCustomer()
                       .getId(),
                booking.getOwner()
                       .getId()
        );
    }

    public BookingCancelledEvent cancelled(Booking booking) {

        return new BookingCancelledEvent(
                booking.getId(),
                booking.getCustomer()
                       .getId(),
                booking.getOwner()
                       .getId()
        );
    }

}
