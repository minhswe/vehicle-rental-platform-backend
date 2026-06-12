package com.rentalplatform.backend.booking.service;

import com.rentalplatform.backend.booking.dto.request.CreateBookingRequest;
import com.rentalplatform.backend.booking.dto.response.BookingResponse;
import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.entity.BookingStatusLog;
import com.rentalplatform.backend.booking.enums.BookingStatus;
import com.rentalplatform.backend.booking.mapper.BookingMapper;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.booking.repository.BookingStatusLogRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.service.OwnerContextService;
import com.rentalplatform.backend.payment.service.PaymentService;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.repository.UserRepository;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private OwnerContextService ownerContextService;

    @Mock
    private BookingStatusLogRepository bookingStatusLogRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UUID userId;
    private UUID ownerUserId;
    private UUID ownerId;
    private UUID bookingId;

    private User customer;
    private User ownerUser;
    private VehicleOwner owner;
    private Vehicle vehicle;
    private Booking booking;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        customer = new User();
        customer.setId(userId);

        ownerUser = new User();
        ownerUser.setId(ownerUserId);

        owner = new VehicleOwner();
        owner.setId(ownerId);
        owner.setUser(ownerUser);

        vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setPricePerDay(BigDecimal.valueOf(100));
        vehicle.setDepositAmount(BigDecimal.valueOf(50));
        vehicle.setVehicleOwner(owner);

        booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(customer);
        booking.setOwner(owner);
        booking.setVehicle(vehicle);
        booking.setBookingStatus(BookingStatus.PENDING);

        bookingResponse = new BookingResponse();
    }

    @Test
    @DisplayName("Should create booking successfully when request is valid")
    void createBooking_ShouldCreateBookingSuccessfully() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(3));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        when(
                bookingRepository
                        .existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        BookingResponse result =
                bookingService.createBooking(request);

        assertNotNull(result);

        verify(bookingRepository)
                .save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw USER_NOT_FOUND when customer does not exist")
    void createBooking_ShouldThrowUserNotFound() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> bookingService.createBooking(request)
                );

        assertEquals(
                ErrorCode.USER_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw VEHICLE_NOT_FOUND when vehicle does not exist")
    void createBooking_ShouldThrowVehicleNotFound() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        request.setVehicleId(UUID.randomUUID());

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(any()))
                .thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> bookingService.createBooking(request)
                );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw OWNER_CANNOT_BOOK_OWN_VEHICLE when owner books own vehicle")
    void createBooking_ShouldThrowOwnerCannotBookOwnVehicle() {

        CreateBookingRequest request = new CreateBookingRequest();
        request.setVehicleId(vehicle.getId());

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(ownerUserId);

        when(userRepository.findById(ownerUserId))
                .thenReturn(Optional.of(ownerUser));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(
                ErrorCode.OWNER_CANNOT_BOOK_OWN_VEHICLE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw INVALID_TIME_RANGE when end time is before start time")
    void createBooking_ShouldThrowInvalidTimeRange() {

        CreateBookingRequest request = new CreateBookingRequest();
        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(
                ErrorCode.INVALID_TIME_RANGE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw INVALID_START_TIME when start time is in the past")
    void createBooking_ShouldThrowInvalidStartTime() {

        CreateBookingRequest request = new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().minusHours(1));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(
                ErrorCode.INVALID_START_TIME,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw VEHICLE_ALREADY_BOOKED_IN_THIS_TIME_RANGE when booking overlaps")
    void createBooking_ShouldThrowVehicleAlreadyBooked() {

        CreateBookingRequest request = new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(3));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        when(
                bookingRepository
                        .existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(
                ErrorCode.VEHICLE_ALREADY_BOOKED_IN_THIS_TIME_RANGE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should return customer bookings page")
    void getMyBookings_ShouldReturnBookings() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Booking> bookings =
                new PageImpl<>(List.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(
                bookingRepository.findByCustomerId(
                        userId,
                        pageable
                )
        ).thenReturn(bookings);

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        Page<BookingResponse> result =
                bookingService.getMyBookings(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return owner bookings page")
    void getOwnerBookings_ShouldReturnBookings() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Booking> bookings =
                new PageImpl<>(List.of(booking));

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(
                bookingRepository.findByOwnerId(
                        ownerId,
                        pageable
                )
        ).thenReturn(bookings);

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        Page<BookingResponse> result =
                bookingService.getOwnerBookings(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return booking when customer accesses own booking")
    void getBooking_ShouldReturnBookingForCustomer() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(bookingMapper.toResponse(booking))
                .thenReturn(bookingResponse);

        BookingResponse result =
                bookingService.getBooking(bookingId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return booking when owner accesses booking")
    void getBooking_ShouldReturnBookingForOwner() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(ownerUserId);

        when(bookingMapper.toResponse(booking))
                .thenReturn(bookingResponse);

        BookingResponse result =
                bookingService.getBooking(bookingId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw BOOKING_NOT_FOUND when booking does not exist")
    void getBooking_ShouldThrowBookingNotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.getBooking(bookingId)
        );

        assertEquals(
                ErrorCode.BOOKING_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_ACCESS_DENIED when user is not booking owner")
    void getBooking_ShouldThrowAccessDenied() {

        UUID strangerId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(strangerId);

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> bookingService.getBooking(bookingId)
                );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should confirm booking successfully")
    void confirmBooking_ShouldConfirmBooking() {

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(
                bookingRepository
                        .findByIdAndOwnerId(
                                bookingId,
                                ownerId
                        )
        ).thenReturn(Optional.of(booking));

        when(bookingRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        BookingResponse result =
                bookingService.confirmBooking(bookingId);

        assertNotNull(result);

        assertEquals(
                BookingStatus.CONFIRMED,
                booking.getBookingStatus()
        );

        verify(bookingStatusLogRepository)
                .save(any(BookingStatusLog.class));
    }

    @Test
    @DisplayName("Should throw INVALID_BOOKING_STATUS when booking is not pending")
    void confirmBooking_ShouldThrowInvalidStatus() {

        booking.setBookingStatus(
                BookingStatus.REJECTED
        );

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(
                bookingRepository
                        .findByIdAndOwnerId(
                                bookingId,
                                ownerId
                        )
        ).thenReturn(Optional.of(booking));

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> bookingService.confirmBooking(bookingId)
                );

        assertEquals(
                ErrorCode.INVALID_BOOKING_STATUS,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should create status log when booking is confirmed")
    void confirmBooking_ShouldCreateStatusLog() {

        UUID ownerUserId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setId(ownerUserId);

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);
        owner.setUser(ownerUser);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setOwner(owner);
        booking.setBookingStatus(BookingStatus.PENDING);

        BookingResponse response = new BookingResponse();

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(bookingRepository.findByIdAndOwnerId(
                bookingId,
                ownerId
        )).thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(ownerUserId);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        bookingService.confirmBooking(bookingId);

        ArgumentCaptor<BookingStatusLog> logCaptor =
                ArgumentCaptor.forClass(BookingStatusLog.class);

        verify(bookingStatusLogRepository)
                .save(logCaptor.capture());

        BookingStatusLog savedLog =
                logCaptor.getValue();

        assertEquals(
                BookingStatus.PENDING,
                savedLog.getOldStatus()
        );

        assertEquals(
                BookingStatus.CONFIRMED,
                savedLog.getNewStatus()
        );

        assertEquals(
                booking,
                savedLog.getBooking()
        );

        assertEquals(
                ownerUserId,
                savedLog.getChangedBy()
        );
    }

    @Test
    @DisplayName("Should reject booking successfully")
    void rejectBooking_ShouldRejectBooking() {

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(bookingRepository.findByIdAndOwnerId(
                bookingId,
                ownerId
        )).thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(bookingResponse);

        BookingResponse result =
                bookingService.rejectBooking(bookingId);

        assertNotNull(result);

        assertEquals(
                BookingStatus.REJECTED,
                booking.getBookingStatus()
        );

        verify(paymentService)
                .refundBookingPayment(booking);

        verify(bookingRepository)
                .save(booking);

        verify(bookingStatusLogRepository)
                .save(any(BookingStatusLog.class));
    }
    @Test
    @DisplayName("Should throw INVALID_BOOKING_STATUS when booking is not pending")
    void rejectBooking_ShouldThrowInvalidStatus() {

        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(
                bookingRepository.findByIdAndOwnerId(
                        bookingId,
                        ownerId
                )
        ).thenReturn(Optional.of(booking));

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.rejectBooking(bookingId)
        );

        assertEquals(
                ErrorCode.INVALID_BOOKING_STATUS,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should create status log when booking is rejected")
    void rejectBooking_ShouldCreateStatusLog() {

        UUID ownerUserId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setId(ownerUserId);

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);
        owner.setUser(ownerUser);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setOwner(owner);
        booking.setBookingStatus(BookingStatus.PENDING);

        BookingResponse response = new BookingResponse();

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(bookingRepository.findByIdAndOwnerId(
                bookingId,
                ownerId
        )).thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(ownerUserId);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        bookingService.rejectBooking(bookingId);

        verify(paymentService)
                .refundBookingPayment(booking);

        ArgumentCaptor<BookingStatusLog> logCaptor =
                ArgumentCaptor.forClass(BookingStatusLog.class);

        verify(bookingStatusLogRepository)
                .save(logCaptor.capture());

        BookingStatusLog savedLog =
                logCaptor.getValue();

        assertEquals(
                BookingStatus.PENDING,
                savedLog.getOldStatus()
        );

        assertEquals(
                BookingStatus.REJECTED,
                savedLog.getNewStatus()
        );

        assertEquals(
                booking,
                savedLog.getBooking()
        );

        assertEquals(
                ownerUserId,
                savedLog.getChangedBy()
        );

        assertNotNull(
                savedLog.getChangedAt()
        );
    }

    @Test
    @DisplayName("Should cancel booking successfully when customer requests cancellation")
    void cancelBooking_ShouldCancelBookingByCustomer() {

        booking.setBookingStatus(
                BookingStatus.CONFIRMED
        );

        when(
                bookingRepository.findById(
                        bookingId
                )
        ).thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(bookingRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        BookingResponse result =
                bookingService.cancelBooking(bookingId);

        assertNotNull(result);

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getBookingStatus()
        );
    }

    @Test
    @DisplayName("Should cancel booking successfully when owner requests cancellation")
    void cancelBooking_ShouldCancelBookingByOwner() {

        UUID ownerUserId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setId(ownerUserId);

        VehicleOwner owner = new VehicleOwner();
        owner.setUser(ownerUser);

        User customer = new User();
        customer.setId(UUID.randomUUID());

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(customer);
        booking.setOwner(owner);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        BookingResponse response = new BookingResponse();

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(ownerUserId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        BookingResponse result =
                bookingService.cancelBooking(bookingId);

        assertNotNull(result);

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getBookingStatus()
        );

        verify(bookingRepository)
                .save(booking);

        verify(bookingStatusLogRepository)
                .save(any(BookingStatusLog.class));
    }

    @Test
    @DisplayName("Should throw INVALID_BOOKING_STATUS when booking cannot be cancelled")
    void cancelBooking_ShouldThrowInvalidStatus() {

        booking.setBookingStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        assertEquals(
                ErrorCode.INVALID_BOOKING_STATUS,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_ACCESS_DENIED when user has no access")
    void cancelBooking_ShouldThrowAccessDenied() {

        UUID strangerId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(strangerId);

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should create status log when booking is cancelled")
    void cancelBooking_ShouldCreateStatusLog() {

        UUID customerId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        User customer = new User();
        customer.setId(customerId);

        User ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());

        VehicleOwner owner = new VehicleOwner();
        owner.setUser(ownerUser);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(customer);
        booking.setOwner(owner);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(new BookingResponse());

        bookingService.cancelBooking(bookingId);

        ArgumentCaptor<BookingStatusLog> logCaptor =
                ArgumentCaptor.forClass(BookingStatusLog.class);

        verify(bookingStatusLogRepository)
                .save(logCaptor.capture());

        BookingStatusLog savedLog =
                logCaptor.getValue();

        assertEquals(
                booking,
                savedLog.getBooking()
        );

        assertEquals(
                BookingStatus.CONFIRMED,
                savedLog.getOldStatus()
        );

        assertEquals(
                BookingStatus.CANCELLED,
                savedLog.getNewStatus()
        );

        assertEquals(
                customerId,
                savedLog.getChangedBy()
        );

        assertNotNull(
                savedLog.getChangedAt()
        );
    }

    @Test
    @DisplayName("Should throw VEHICLE_NOT_AVAILABLE when vehicle is not available")
    void createBooking_ShouldThrowVehicleNotAvailable() {

        CreateBookingRequest request = new CreateBookingRequest();
        request.setVehicleId(vehicle.getId());

        vehicle.setStatus(VehicleStatus.RENTED);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        AppException exception = assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_NOT_FOUND when owner booking not found")
    void confirmBooking_ShouldThrowBookingNotFound() {

        when(ownerContextService.getCurrentOwner())
                .thenReturn(owner);

        when(
                bookingRepository.findByIdAndOwnerId(
                        bookingId,
                        ownerId
                )
        ).thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> bookingService.confirmBooking(bookingId)
                );

        assertEquals(
                ErrorCode.BOOKING_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should calculate booking price correctly")
    void createBooking_ShouldCalculatePriceCorrectly() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(4));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        when(
                bookingRepository
                        .existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(bookingRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        bookingService.createBooking(request);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository)
                .save(captor.capture());

        Booking savedBooking =
                captor.getValue();

        assertEquals(3, savedBooking.getTotalDays());

        assertEquals(
                BigDecimal.valueOf(300),
                savedBooking.getRentalPrice()
        );

        assertEquals(
                BigDecimal.valueOf(50),
                savedBooking.getDepositAmount()
        );

        assertEquals(
                BigDecimal.valueOf(350),
                savedBooking.getTotalAmount()
        );

        assertEquals(
                BookingStatus.PENDING,
                savedBooking.getBookingStatus()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_NOT_FOUND when cancelling unknown booking")
    void cancelBooking_ShouldThrowBookingNotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> bookingService.cancelBooking(bookingId)
                );

        assertEquals(
                ErrorCode.BOOKING_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should set minimum booking days to one when rental duration is less than one day")
    void createBooking_ShouldUseMinimumOneDay() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());

        LocalDateTime start =
                LocalDateTime.now().plusDays(1);

        LocalDateTime end =
                start.plusHours(5);

        request.setStartTime(start);
        request.setEndTime(end);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        when(
                bookingRepository
                        .existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(bookingRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toResponse(any()))
                .thenReturn(bookingResponse);

        bookingService.createBooking(request);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository)
                .save(captor.capture());

        Booking savedBooking =
                captor.getValue();

        assertEquals(
                1,
                savedBooking.getTotalDays()
        );

        assertEquals(
                vehicle.getPricePerDay(),
                savedBooking.getRentalPrice()
        );

        assertEquals(
                vehicle.getPricePerDay()
                       .add(vehicle.getDepositAmount()),
                savedBooking.getTotalAmount()
        );
    }

    @Test
    @DisplayName("Should not save booking when time range is invalid")
    void createBooking_ShouldNotSaveWhenTimeRangeInvalid() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());

        request.setStartTime(
                LocalDateTime.now().plusDays(2)
        );

        request.setEndTime(
                LocalDateTime.now().plusDays(1)
        );

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        verify(
                bookingRepository,
                never()
        ).save(any());
    }

    @Test
    @DisplayName("Should not save booking when vehicle already booked")
    void createBooking_ShouldNotSaveWhenVehicleAlreadyBooked() {

        CreateBookingRequest request =
                new CreateBookingRequest();

        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(3));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        when(vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));

        when(
                bookingRepository
                        .existsByVehicleIdAndBookingStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(true);

        assertThrows(
                AppException.class,
                () -> bookingService.createBooking(request)
        );

        verify(
                bookingRepository,
                never()
        ).save(any());
    }
}
