package com.rentalplatform.backend.payment.mapper;

import com.rentalplatform.backend.payment.dto.request.PaymentRequest;
import com.rentalplatform.backend.payment.dto.response.PaymentResponse;
import com.rentalplatform.backend.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentResponse toResponse(Payment payment);
}
