package com.rentalplatform.backend.booking.controller;

import com.rentalplatform.backend.booking.dto.response.BookingResponse;
import com.rentalplatform.backend.booking.service.BookingService;
import com.rentalplatform.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/bookings")
public class OwnerBookingController {
    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<Page<BookingResponse>> getOwnerBookings(
            Pageable pageable
    ) {

        return ApiResponse.success(
                bookingService.getOwnerBookings(pageable)
        );
    }

    @PatchMapping("/{bookingId}/confirm")
    public ApiResponse<BookingResponse> confirmBooking(
            @PathVariable UUID bookingId
    ) {

        return ApiResponse.success(
                bookingService.confirmBooking(bookingId)
        );
    }

    @PatchMapping("/{bookingId}/reject")
    public ApiResponse<BookingResponse> rejectBooking(
            @PathVariable UUID bookingId
    ) {

        return ApiResponse.success(
                bookingService.rejectBooking(bookingId)
        );
    }
}
