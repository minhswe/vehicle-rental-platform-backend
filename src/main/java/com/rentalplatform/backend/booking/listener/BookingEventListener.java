package com.rentalplatform.backend.booking.listener;

import com.rentalplatform.backend.booking.event.BookingCancelledEvent;
import com.rentalplatform.backend.booking.event.BookingConfirmedEvent;
import com.rentalplatform.backend.booking.event.BookingCreatedEvent;
import com.rentalplatform.backend.booking.event.BookingRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingEventListener {

    @EventListener
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("""
                Booking created
                bookingId={}
                customerId={}
                ownerId={}
                vehicleId={}
                totalAmount={}
                """,
                 event.getBookingId(),
                 event.getCustomerId(),
                 event.getOwnerId(),
                 event.getVehicleId(),
                 event.getTotalAmount()
        );


        // notificationService.notifyOwner(...)
        // emailService.sendBookingCreated(...)
    }

    @EventListener
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("""
                Booking confirmed
                bookingId={}
                customerId={}
                ownerId={}
                """,
                 event.getBookingId(),
                 event.getCustomerId(),
                 event.getOwnerId()
        );
        // notificationService.notifyCustomer(...)
    }

    @EventListener
    public void handleBookingRejected(BookingRejectedEvent event) {
        log.info("""
                Booking rejected
                bookingId={}
                customerId={}
                ownerId={}
                """,
                 event.getBookingId(),
                 event.getCustomerId(),
                 event.getOwnerId()
        );


        // notificationService.notifyCustomer(...)
    }

    @EventListener
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("""
                Booking cancelled
                bookingId={}
                customerId={}
                ownerId={}
                """,
                 event.getBookingId(),
                 event.getCustomerId(),
                 event.getOwnerId()
        );

        // refundService.refund(...)
        // notificationService.notifyOwner(...)
    }


}
