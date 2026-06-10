package com.rentalplatform.backend.booking.entity;

import com.rentalplatform.backend.booking.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking_status_logs")
@Getter
@Setter
public class BookingStatusLog {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Enumerated(EnumType.STRING)
    private BookingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private BookingStatus newStatus;

    private UUID changedBy;

    private Instant changedAt;


}
