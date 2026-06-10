package com.rentalplatform.backend.booking.controller;

import com.rentalplatform.backend.booking.dto.request.CreateBookingRequest;
import com.rentalplatform.backend.booking.dto.response.BookingResponse;
import com.rentalplatform.backend.booking.service.BookingService;
import com.rentalplatform.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request
    ) {

        return ApiResponse.success(
                bookingService.createBooking(request)
        );
    }

    @GetMapping("/me")
    public ApiResponse<Page<BookingResponse>> getMyBookings(
            Pageable pageable
    ) {

        return ApiResponse.success(
                bookingService.getMyBookings(pageable)
        );
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(
            @PathVariable UUID bookingId
    ) {

        return ApiResponse.success(
                bookingService.getBooking(bookingId)
        );
    }

    @PatchMapping("/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable UUID bookingId
    ) {

        return ApiResponse.success(
                bookingService.cancelBooking(bookingId)
        );
    }
}
