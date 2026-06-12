package com.rentalplatform.backend.payment.service;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.payment.dto.request.PaymentRequest;
import com.rentalplatform.backend.payment.dto.response.PaymentResponse;

import java.util.UUID;


public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse getPaymentByBookingId(UUID bookingId);

    PaymentResponse pay(UUID paymentId);

    PaymentResponse cancel(UUID paymentId);

    void refundBookingPayment(Booking booking);

}
