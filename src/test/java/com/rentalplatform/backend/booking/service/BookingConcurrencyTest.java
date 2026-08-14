package com.rentalplatform.backend.booking.service;

import com.rentalplatform.backend.booking.dto.request.CreateBookingRequest;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.repository.OwnerRepository;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.repository.UserRepository;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerRepository vehicleOwnerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    private User customer;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        // Clean up before starting
        bookingRepository.deleteAll();
        vehicleRepository.deleteAll();
        vehicleOwnerRepository.deleteAll();
        userRepository.deleteAll();

        // Create Customer
        customer = User.builder()
                .email("customer@test.com")
                .password("password")
                .build();
        customer = userRepository.save(customer);

        // Create Owner User
        User ownerUser = User.builder()
                .email("owner@test.com")
                .password("password")
                .build();
        ownerUser = userRepository.save(ownerUser);

        // Create Vehicle Owner
        VehicleOwner owner = new VehicleOwner();
        owner.setUser(ownerUser);
        owner.setBusinessName("Test Business");
        owner.setVerifiedStatus("VERIFIED");
        owner.setRatingAvg(BigDecimal.ZERO);
        owner.setTotalVehicles(1);
        owner = vehicleOwnerRepository.save(owner);

        // Create Vehicle
        vehicle = new Vehicle();
        vehicle.setVehicleOwner(owner);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Camry");
        vehicle.setYear(2023);
        vehicle.setLicensePlate("TEST-1234");
        vehicle.setMileage(100);
        vehicle.setPricePerDay(BigDecimal.valueOf(100));
        vehicle.setDepositAmount(BigDecimal.valueOf(500));
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle = vehicleRepository.save(vehicle);

        when(authenticationFacade.getCurrentUserId()).thenReturn(customer.getId());
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        vehicleRepository.deleteAll();
        vehicleOwnerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testConcurrentBookingCreation() throws InterruptedException {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setVehicleId(vehicle.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(3));

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // wait until all threads are ready
                    bookingService.createBooking(request);
                    successCount.incrementAndGet();
                } catch (AppException e) {
                    if (e.getErrorCode() == ErrorCode.CONCURRENT_BOOKING_CONFLICT || 
                        e.getErrorCode() == ErrorCode.VEHICLE_ALREADY_BOOKED_IN_THIS_TIME_RANGE) {
                        conflictCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Let all threads start at once
        doneLatch.await(); // Wait for all threads to finish

        assertEquals(1, successCount.get(), "Only one booking should succeed");
        assertEquals(numberOfThreads - 1, conflictCount.get(), "Other bookings should fail with conflict");
        assertEquals(1, bookingRepository.count(), "Only one booking should be created in the database");
    }
}
