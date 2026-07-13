package com.rentalplatform.backend.booking.factory;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.event.BookingCancelledEvent;
import com.rentalplatform.backend.booking.event.BookingConfirmedEvent;
import com.rentalplatform.backend.booking.event.BookingCreatedEvent;
import com.rentalplatform.backend.booking.event.BookingRejectedEvent;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingEventFactoryTest {

    private BookingEventFactory factory;

    private Booking booking;
    private UUID bookingId;
    private UUID customerId;
    private UUID ownerId;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {

        factory = new BookingEventFactory();

        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();

        User customer = new User();
        customer.setId(customerId);

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);

        booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(customer);
        booking.setOwner(owner);
        booking.setVehicle(vehicle);
        booking.setTotalAmount(
                BigDecimal.valueOf(500_000)
        );
    }

    @Test
    @DisplayName("Should create BookingCreatedEvent from booking")
    void created_ShouldReturnBookingCreatedEvent() {

        BookingCreatedEvent event =
                factory.created(booking);

        assertAll(
                () -> assertEquals(
                        bookingId,
                        event.getBookingId()
                ),
                () -> assertEquals(
                        customerId,
                        event.getCustomerId()
                ),
                () -> assertEquals(
                        ownerId,
                        event.getOwnerId()
                ),
                () -> assertEquals(
                        vehicleId,
                        event.getVehicleId()
                ),
                () -> assertEquals(
                        BigDecimal.valueOf(500_000),
                        event.getTotalAmount()
                ),
                () -> assertNotNull(
                        event.getEventId()
                ),
                () -> assertNotNull(
                        event.getOccurredAt()
                )
        );
    }

    @Test
    @DisplayName("Should create BookingConfirmedEvent from booking")
    void confirmed_ShouldReturnBookingConfirmedEvent() {

        BookingConfirmedEvent event =
                factory.confirmed(booking);

        assertAll(
                () -> assertEquals(
                        bookingId,
                        event.getBookingId()
                ),
                () -> assertEquals(
                        customerId,
                        event.getCustomerId()
                ),
                () -> assertEquals(
                        ownerId,
                        event.getOwnerId()
                )
        );
    }

    @Test
    @DisplayName("Should create BookingRejectedEvent from booking")
    void rejected_ShouldReturnBookingRejectedEvent() {

        BookingRejectedEvent event =
                factory.rejected(booking);

        assertAll(
                () -> assertEquals(
                        bookingId,
                        event.getBookingId()
                ),
                () -> assertEquals(
                        customerId,
                        event.getCustomerId()
                ),
                () -> assertEquals(
                        ownerId,
                        event.getOwnerId()
                )
        );
    }

    @Test
    @DisplayName("Should create BookingCancelledEvent from booking")
    void cancelled_ShouldReturnBookingCancelledEvent() {

        BookingCancelledEvent event =
                factory.cancelled(booking);

        assertAll(
                () -> assertEquals(
                        bookingId,
                        event.getBookingId()
                ),
                () -> assertEquals(
                        customerId,
                        event.getCustomerId()
                ),
                () -> assertEquals(
                        ownerId,
                        event.getOwnerId()
                )
        );
    }
}
