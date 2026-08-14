package com.rentalplatform.backend.booking.service;

import com.rentalplatform.backend.booking.dto.request.CreateBookingRequest;
import com.rentalplatform.backend.booking.dto.response.BookingResponse;
import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.entity.BookingStatusLog;
import com.rentalplatform.backend.booking.constant.BookingStatus;
import com.rentalplatform.backend.booking.factory.BookingEventFactory;
import com.rentalplatform.backend.booking.mapper.BookingMapper;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.booking.repository.BookingStatusLogRepository;
import com.rentalplatform.backend.common.event.DomainEventPublisher;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.owner.service.OwnerContextService;
import com.rentalplatform.backend.payment.service.PaymentService;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.repository.UserRepository;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final AuthenticationFacade authenticationFacade;
    private final OwnerContextService ownerContextService;
    private final BookingStatusLogRepository bookingStatusLogRepository;
    private final PaymentService paymentService;

    private final BookingEventFactory bookingEventFactory;

    private final DomainEventPublisher eventPublisher;

    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(
                    BookingStatus.PENDING,
                    BookingStatus.CONFIRMED,
                    BookingStatus.IN_PROGRESS
            );

    private static final Set<BookingStatus> CANCELLABLE_STATUSES =
            Set.of(
                    BookingStatus.PENDING,
                    BookingStatus.CONFIRMED
            );


    @Transactional
    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {

        UUID customerId =
                authenticationFacade.getCurrentUserId();

        // Get customer
        User customer = userRepository.findById(customerId)
                                      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // get vehicle
        Vehicle vehicle;
        try {
            vehicle = vehicleRepository.findByIdWithLock(request.getVehicleId())
                                       .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
        } catch (PessimisticLockingFailureException | QueryTimeoutException e) {
            throw new AppException(ErrorCode.CONCURRENT_BOOKING_CONFLICT);
        }

        validateBookingRequest(request, vehicle, customerId);

        //CHECK OVERLAPPING BOOKINGS
        validateVehicleAvailability(
                vehicle.getId(),
                request.getStartTime(),
                request.getEndTime()
        );

        // Calculate days
        long days = ChronoUnit.DAYS.between(
                request.getStartTime()
                       .toLocalDate(),
                request.getEndTime()
                       .toLocalDate()
        );

        days = Math.max(days, 1);

        // 8. Calculate price
        BigDecimal rentalPrice = vehicle.getPricePerDay()
                                        .multiply(BigDecimal.valueOf(days));

        BigDecimal deposit = vehicle.getDepositAmount();

        BigDecimal total = rentalPrice.add(deposit);

        // 9. Build booking
        Booking booking = buildBooking(
                customer,
                vehicle,
                request,
                days,
                rentalPrice,
                deposit,
                total
        );

        // 10. Save
        Booking saved = bookingRepository.save(booking);

        eventPublisher.publish(
                bookingEventFactory.created(saved)
        );

        return bookingMapper.toResponse(saved);
    }

    @Override
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        UUID customerId =
                authenticationFacade.getCurrentUserId();


        return bookingRepository
                .findByCustomerId(
                        customerId,
                        pageable
                )
                .map(bookingMapper::toResponse);
    }

    @Override
    public Page<BookingResponse> getOwnerBookings(Pageable pageable) {

        UUID ownerId = ownerContextService.getCurrentOwner()
                                          .getId();

        return bookingRepository
                .findByOwnerId(ownerId, pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    public BookingResponse getBooking(UUID bookingId) {

        Booking booking = getBookingById(bookingId);


        validateBookingAccess(
                booking,
                authenticationFacade.getCurrentUserId()
        );


        return bookingMapper.toResponse(booking);
    }

    private Booking getOwnerBooking(UUID bookingId) {

        UUID ownerId =
                ownerContextService
                        .getCurrentOwner()
                        .getId();

        return bookingRepository
                .findByIdAndOwnerId(
                        bookingId,
                        ownerId
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.BOOKING_NOT_FOUND
                        )
                );
    }

    @Transactional
    @Override
    public BookingResponse confirmBooking(UUID bookingId) {

        Booking booking = getOwnerBooking(bookingId);

        validatePendingBooking(booking);

        Booking updatedBooking = updateBookingStatus(
                booking,
                BookingStatus.CONFIRMED
        );
        eventPublisher.publish(bookingEventFactory.confirmed(updatedBooking));

        return bookingMapper.toResponse(
                updatedBooking
        );
    }

    @Transactional
    @Override
    public BookingResponse rejectBooking(UUID bookingId) {

        Booking booking = getOwnerBooking(bookingId);

        validatePendingBooking(booking);

        Booking updatedBooking =
                updateBookingStatus(
                        booking,
                        BookingStatus.REJECTED
                );

        paymentService.refundBookingPayment(
                updatedBooking
        );

        eventPublisher.publish(bookingEventFactory.rejected(updatedBooking));

        return bookingMapper.toResponse(
                updatedBooking);
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(UUID bookingId) {

        Booking booking = getBookingById(bookingId);

        validateBookingAccess(
                booking,
                authenticationFacade.getCurrentUserId()
        );

        if (!CANCELLABLE_STATUSES.contains(
                booking.getBookingStatus()
        )) {
            throw new AppException(
                    ErrorCode.INVALID_BOOKING_STATUS
            );
        }

        Booking updatedBooking = updateBookingStatus(booking, BookingStatus.CANCELLED);

        eventPublisher.publish(bookingEventFactory.cancelled(updatedBooking));

        return bookingMapper.toResponse(
                updatedBooking
        );
    }

    private void saveStatusLog(
            Booking booking,
            BookingStatus oldStatus,
            BookingStatus newStatus
    ) {

        BookingStatusLog log =
                new BookingStatusLog();

        log.setBooking(booking);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangedAt(Instant.now());
        log.setChangedBy(authenticationFacade.getCurrentUserId());

        bookingStatusLogRepository.save(log);
    }

    private void validatePendingBooking(
            Booking booking
    ) {

        if (booking.getBookingStatus()
            != BookingStatus.PENDING) {

            throw new AppException(
                    ErrorCode.INVALID_BOOKING_STATUS
            );
        }
    }

    private void validateBookingAccess(
            Booking booking,
            UUID userId
    ) {

        boolean isCustomer =
                booking.getCustomer()
                       .getId()
                       .equals(userId);

        boolean isOwner =
                booking.getOwner()
                       .getUser()
                       .getId()
                       .equals(userId);

        if (!isCustomer && !isOwner) {
            throw new AppException(
                    ErrorCode.BOOKING_ACCESS_DENIED
            );
        }
    }

    private Booking buildBooking(
            User customer,
            Vehicle vehicle,
            CreateBookingRequest request,
            long days,
            BigDecimal rentalPrice,
            BigDecimal deposit,
            BigDecimal total
    ) {

        Booking booking = new Booking();

        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setOwner(vehicle.getVehicleOwner());

        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());

        booking.setTotalDays((int) days);
        booking.setRentalPrice(rentalPrice);
        booking.setDepositAmount(deposit);
        booking.setTotalAmount(total);

        booking.setBookingStatus(BookingStatus.PENDING);

        return booking;
    }

    private void validateBookingRequest(
            CreateBookingRequest request,
            Vehicle vehicle,
            UUID customerId
    ) {

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new AppException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        if (vehicle.getVehicleOwner()
                   .getUser()
                   .getId()
                   .equals(customerId)) {
            throw new AppException(ErrorCode.OWNER_CANNOT_BOOK_OWN_VEHICLE);
        }

        if (!request.getEndTime()
                    .isAfter(request.getStartTime())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }

        if (request.getStartTime()
                   .isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_START_TIME);
        }
    }

    private void validateVehicleAvailability(
            UUID vehicleId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {

        boolean hasConflict =
                bookingRepository
                        .existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                vehicleId,
                                BLOCKING_STATUSES,
                                endTime,
                                startTime
                        );

        if (hasConflict) {
            throw new AppException(
                    ErrorCode.VEHICLE_ALREADY_BOOKED_IN_THIS_TIME_RANGE
            );
        }
    }

    private Booking updateBookingStatus(
            Booking booking,
            BookingStatus newStatus
    ) {

        BookingStatus oldStatus = booking.getBookingStatus();

        booking.setBookingStatus(newStatus);

        saveStatusLog(
                booking,
                oldStatus,
                newStatus
        );

        return bookingRepository.save(booking);
    }

    private Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                                .orElseThrow(
                                        () -> new AppException(
                                                ErrorCode.BOOKING_NOT_FOUND
                                        )
                                );
    }
}
