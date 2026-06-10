package com.rentalplatform.backend.booking.service;

import com.rentalplatform.backend.booking.dto.response.BookingResponse;

import java.util.UUID;

public interface BookingStatusLogService {

    BookingResponse confirmBooking(UUID bookingId);

    BookingResponse rejectBooking(UUID bookingId);

    BookingResponse cancelBooking(UUID bookingId);
}
