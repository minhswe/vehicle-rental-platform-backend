package com.rentalplatform.backend.booking.entity;

import com.rentalplatform.backend.booking.constant.BookingStatus;
import com.rentalplatform.backend.common.entity.AuditEntity;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter

public class Booking extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private VehicleOwner owner;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalDays;

    private BigDecimal rentalPrice;

    private BigDecimal depositAmount;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
