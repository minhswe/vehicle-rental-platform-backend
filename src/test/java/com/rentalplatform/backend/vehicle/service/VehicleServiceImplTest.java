package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.service.OwnerService;
import com.rentalplatform.backend.vehicle.dto.request.CreateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.request.UpdateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.response.VehicleResponse;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.enums.VehicleStatus;
import com.rentalplatform.backend.vehicle.mapper.VehicleMapper;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @DisplayName("Should create vehicle when request is valid")
    @Test
    void createVehicle_ShouldReturnVehicleResponse_WhenValidRequest() {

        CreateVehicleRequest request =
                new CreateVehicleRequest();

        request.setBrand("Toyota");
        request.setModel("Vios");
        request.setYear(2024);
        request.setLicensePlate("72d99999");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        Vehicle savedVehicle = new Vehicle();
        savedVehicle.setId(UUID.randomUUID());

        VehicleResponse response =
                mock(VehicleResponse.class);

        when(vehicleRepository.existsByLicensePlate("72D99999"))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(savedVehicle);

        when(vehicleMapper.toResponse(savedVehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.createVehicle(request);

        assertNotNull(result);

        verify(vehicleRepository)
                .save(any(Vehicle.class));
    }

    @DisplayName("Should throw exception when manufacture year is invalid")
    @Test
    void createVehicle_ShouldThrowException_WhenYearInvalid() {

        CreateVehicleRequest request = new CreateVehicleRequest();

        request.setBrand("Toyota");
        request.setModel("Vios");
        request.setYear(1980);
        request.setLicensePlate("72D99999");

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.createVehicle(request)
        );

        assertEquals(
                ErrorCode.INVALID_VEHICLE_YEAR,
                exception.getErrorCode()
        );

        verify(vehicleRepository, never())
                .save(any());
    }

    @DisplayName("Should normalize license plate before validation and saving")
    @Test
    void createVehicle_ShouldNormalizeLicensePlate() {

        CreateVehicleRequest request = new CreateVehicleRequest();

        request.setBrand("Toyota");
        request.setModel("Vios");
        request.setYear(2024);
        request.setLicensePlate("72d99999");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        Vehicle savedVehicle = new Vehicle();
        savedVehicle.setId(UUID.randomUUID());

        VehicleResponse response = mock(VehicleResponse.class);

        when(vehicleRepository.existsByLicensePlate("72D99999"))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(savedVehicle);

        when(vehicleMapper.toResponse(savedVehicle))
                .thenReturn(response);

        vehicleService.createVehicle(request);

        verify(vehicleRepository)
                .existsByLicensePlate("72D99999");
    }

    @DisplayName("Should throw exception when license plate already exists")
    @Test
    void updateVehicle_ShouldThrowException_WhenLicensePlateAlreadyExists() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        UpdateVehicleRequest request = new UpdateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.of(vehicle));

        when(vehicleRepository.existsByLicensePlateAndIdNot(
                "72D99999",
                vehicleId
        )).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.updateVehicle(vehicleId, request)
        );

        assertEquals(
                ErrorCode.LICENSE_PLATE_ALREADY_EXISTS,
                exception.getErrorCode()
        );
    }

    @DisplayName("Should throw exception when vehicle is not owned by current owner")
    @Test
    void updateVehicle_ShouldThrowException_WhenVehicleNotOwned() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.empty());

        UpdateVehicleRequest request = new UpdateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.updateVehicle(vehicleId, request)
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @DisplayName("Throw exception when deleting vehicle not owned by current owner")
    @Test
    void softDeleteVehicle_ShouldThrowException_WhenVehicleNotOwned() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.softDeleteVehicle(vehicleId)
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @DisplayName("Should set default values when creating vehicle")
    @Test
    void createVehicle_ShouldSetDefaultValues() {

        CreateVehicleRequest request = new CreateVehicleRequest();

        request.setBrand("Toyota");
        request.setModel("Vios");
        request.setYear(2024);
        request.setLicensePlate("72D99999");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        Vehicle savedVehicle = new Vehicle();

        when(vehicleRepository.existsByLicensePlate("72D99999"))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(savedVehicle);

        when(vehicleMapper.toResponse(savedVehicle))
                .thenReturn(mock(VehicleResponse.class));

        vehicleService.createVehicle(request);

        ArgumentCaptor<Vehicle> captor =
                ArgumentCaptor.forClass(Vehicle.class);

        verify(vehicleRepository)
                .save(captor.capture());

        Vehicle captured = captor.getValue();

        assertEquals(
                VehicleStatus.AVAILABLE,
                captured.getStatus()
        );

        assertFalse(captured.isDeleted());

        assertEquals(
                owner,
                captured.getVehicleOwner()
        );

        assertNotNull(captured.getCreatedAt());
        assertNotNull(captured.getUpdatedAt());
    }

    @DisplayName("Should throw exception when license plate already exists")
    @Test
    void createVehicle_ShouldThrowException_WhenLicensePlateExists() {

        CreateVehicleRequest request =
                new CreateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        when(vehicleRepository.existsByLicensePlate("72D99999"))
                .thenReturn(true);

        assertThrows(
                AppException.class,
                () -> vehicleService.createVehicle(request)
        );

        verify(vehicleRepository, never())
                .save(any());
    }

    @DisplayName("Should return vehicle details when vehicle exists and is available")
    @Test
    void getVehicleDetail_ShouldReturnVehicle() {

        UUID vehicleId = UUID.randomUUID();

        Vehicle vehicle = new Vehicle();

        VehicleResponse response =
                mock(VehicleResponse.class);

        when(vehicleRepository
                     .findByIdAndStatusAndDeletedFalse(
                             vehicleId,
                             VehicleStatus.AVAILABLE
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.getVehicleDetail(vehicleId);

        assertNotNull(result);
    }

    @DisplayName("Should throw exception when vehicle is not found")
    @Test
    void getVehicleDetail_ShouldThrowException_WhenNotFound() {

        UUID vehicleId = UUID.randomUUID();

        when(vehicleRepository
                     .findByIdAndStatusAndDeletedFalse(
                             vehicleId,
                             VehicleStatus.AVAILABLE
                     ))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> vehicleService.getVehicleDetail(vehicleId)
        );
    }

    @DisplayName("Should update vehicle when request is valid")
    @Test
    void updateVehicle_ShouldUpdateVehicle() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner =
                new VehicleOwner();

        owner.setId(ownerId);

        Vehicle vehicle =
                new Vehicle();

        vehicle.setId(vehicleId);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        UpdateVehicleRequest request =
                new UpdateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        VehicleResponse response =
                mock(VehicleResponse.class);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             ownerId
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleRepository
                     .existsByLicensePlateAndIdNot(
                             "72D99999",
                             vehicleId
                     ))
                .thenReturn(false);

        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.updateVehicle(
                        vehicleId,
                        request
                );

        assertNotNull(result);

        verify(vehicleMapper)
                .updateVehicle(request, vehicle);
    }

    @DisplayName("Should throw exception when updating rented vehicle")
    @Test
    void updateVehicle_ShouldThrowException_WhenVehicleRented() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner =
                new VehicleOwner();

        owner.setId(ownerId);

        Vehicle vehicle =
                new Vehicle();

        vehicle.setId(vehicleId);
        vehicle.setStatus(VehicleStatus.RENTED);

        UpdateVehicleRequest request =
                new UpdateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             ownerId
                     ))
                .thenReturn(Optional.of(vehicle));

        assertThrows(
                AppException.class,
                () -> vehicleService.updateVehicle(
                        vehicleId,
                        request
                )
        );
    }

    @DisplayName("Should soft delete vehicle when vehicle is available")
    @Test
    void softDeleteVehicle_ShouldMarkDeleted() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner =
                new VehicleOwner();

        owner.setId(ownerId);

        Vehicle vehicle =
                new Vehicle();

        vehicle.setStatus(VehicleStatus.AVAILABLE);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             ownerId
                     ))
                .thenReturn(Optional.of(vehicle));

        vehicleService.softDeleteVehicle(vehicleId);

        assertTrue(vehicle.isDeleted());

        verify(vehicleRepository)
                .save(vehicle);
    }

    @DisplayName("Should throw exception when deleting rented vehicle")
    @Test
    void softDeleteVehicle_ShouldThrowException_WhenVehicleRented() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner =
                new VehicleOwner();

        owner.setId(ownerId);

        Vehicle vehicle =
                new Vehicle();

        vehicle.setStatus(VehicleStatus.RENTED);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             ownerId
                     ))
                .thenReturn(Optional.of(vehicle));

        assertThrows(
                AppException.class,
                () -> vehicleService.softDeleteVehicle(vehicleId)
        );
    }
}