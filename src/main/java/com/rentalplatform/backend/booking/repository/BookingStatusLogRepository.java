package com.rentalplatform.backend.booking.repository;

import com.rentalplatform.backend.booking.entity.BookingStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingStatusLogRepository extends JpaRepository<BookingStatusLog, UUID> {

}
