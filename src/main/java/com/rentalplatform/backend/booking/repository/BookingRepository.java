package com.rentalplatform.backend.booking.repository;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID vehicleId,
            Collection<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    Page<Booking> findByOwnerId(
            UUID ownerId,
            Pageable pageable
    );

    Page<Booking> findByCustomerId(UUID id, Pageable pageable);

    Optional<Booking> findById(UUID id);

    Optional<Booking> findByIdAndOwnerId(UUID id, UUID ownerId);

}
