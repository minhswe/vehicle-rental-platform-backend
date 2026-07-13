package com.rentalplatform.backend.booking.dto.response;

import com.rentalplatform.backend.booking.constant.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BookingResponse {

    private UUID id;

    private UUID customerId;

    private UUID vehicleId;

    private UUID ownerId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalDays;

    private BigDecimal rentalPrice;

    private BigDecimal depositAmount;

    private BigDecimal totalAmount;

    private BookingStatus bookingStatus;

    private Instant createdAt;
}
