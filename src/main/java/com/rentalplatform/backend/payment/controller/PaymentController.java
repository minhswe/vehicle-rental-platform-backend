package com.rentalplatform.backend.payment.controller;

import com.rentalplatform.backend.common.response.ApiResponse;
import com.rentalplatform.backend.payment.dto.request.PaymentRequest;
import com.rentalplatform.backend.payment.dto.response.PaymentResponse;
import com.rentalplatform.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {

        return ApiResponse.success(
                paymentService.createPayment(request),
                "Payment created successfully"
        );
    }

    @GetMapping("/booking/{bookingId}")
    public ApiResponse<PaymentResponse> getPaymentByBookingId(
            @PathVariable UUID bookingId
    ) {

        return ApiResponse.success(
                paymentService.getPaymentByBookingId(bookingId)
        );
    }

    @PostMapping("/{paymentId}/pay")
    public ApiResponse<PaymentResponse> pay(
            @PathVariable UUID paymentId
    ) {

        return ApiResponse.success(
                paymentService.pay(paymentId),
                "Payment completed successfully"
        );
    }

    @PostMapping("/{paymentId}/cancel")
    public ApiResponse<PaymentResponse> cancel(
            @PathVariable UUID paymentId
    ) {

        return ApiResponse.success(
                paymentService.cancel(paymentId),
                "Payment cancelled successfully"
        );
    }
}
