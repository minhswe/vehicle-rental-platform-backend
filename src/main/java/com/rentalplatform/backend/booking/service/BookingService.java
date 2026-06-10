package com.rentalplatform.backend.booking.service;

import com.rentalplatform.backend.booking.dto.request.CreateBookingRequest;
import com.rentalplatform.backend.booking.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    Page<BookingResponse> getMyBookings(Pageable pageable);

    Page<BookingResponse> getOwnerBookings(Pageable pageable);

    BookingResponse getBooking(UUID bookingId);

    BookingResponse confirmBooking(UUID bookingId);

    BookingResponse rejectBooking(UUID bookingId);

    BookingResponse cancelBooking(UUID bookingId);

}
