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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    @Test
    @DisplayName("Create vehicle successfully")
    void createVehicle_ShouldCreateSuccessfully() {

        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setYear(2024);
        request.setLicensePlate("72d99999");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        VehicleResponse response = mock(VehicleResponse.class);

        when(vehicleRepository.existsByLicensePlateAndDeletedFalse("72D99999"))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.createVehicle(request);

        assertNotNull(result);

        ArgumentCaptor<Vehicle> captor =
                ArgumentCaptor.forClass(Vehicle.class);

        verify(vehicleRepository)
                .save(captor.capture());

        Vehicle captured = captor.getValue();

        assertEquals(owner, captured.getVehicleOwner());
        assertEquals(VehicleStatus.AVAILABLE, captured.getStatus());

        verify(vehicleMapper)
                .toResponse(vehicle);
    }

    @Test
    @DisplayName("Throw exception when vehicle year invalid")
    void createVehicle_ShouldThrow_WhenYearInvalid() {

        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setYear(1980);
        request.setLicensePlate("72D99999");

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> vehicleService.createVehicle(request)
                );

        assertEquals(
                ErrorCode.INVALID_VEHICLE_YEAR,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Throw exception when license plate exists")
    void createVehicle_ShouldThrow_WhenLicensePlateExists() {

        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setYear(2024);
        request.setLicensePlate("72D99999");

        when(vehicleRepository.existsByLicensePlateAndDeletedFalse("72D99999"))
                .thenReturn(true);

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> vehicleService.createVehicle(request)
                );

        assertEquals(
                ErrorCode.LICENSE_PLATE_ALREADY_EXISTS,
                ex.getErrorCode()
        );

        verify(vehicleRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Get vehicle detail successfully")
    void getVehicleDetail_ShouldReturnVehicle() {

        UUID vehicleId = UUID.randomUUID();

        Vehicle vehicle = new Vehicle();

        VehicleResponse response =
                mock(VehicleResponse.class);

        when(vehicleRepository.findByIdAndStatusAndDeletedFalse(
                vehicleId,
                VehicleStatus.AVAILABLE
        )).thenReturn(Optional.of(vehicle));

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.getVehicleDetail(vehicleId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Throw exception when vehicle not found")
    void getVehicleDetail_ShouldThrow_WhenNotFound() {

        UUID vehicleId = UUID.randomUUID();

        when(vehicleRepository.findByIdAndStatusAndDeletedFalse(
                vehicleId,
                VehicleStatus.AVAILABLE
        )).thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> vehicleService.getVehicleDetail(vehicleId)
        );
    }

    @Test
    @DisplayName("Update vehicle successfully")
    void updateVehicle_ShouldUpdateSuccessfully() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();
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

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.of(vehicle));

        when(vehicleRepository.existsByLicensePlateAndIdNotAndDeletedFalse(
                "72D99999",
                vehicleId
        )).thenReturn(false);

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.updateVehicle(vehicleId, request);

        assertNotNull(result);

        verify(vehicleMapper)
                .updateVehicle(request, vehicle);

        verify(vehicleRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Throw exception when updating rented vehicle")
    void updateVehicle_ShouldThrow_WhenVehicleRented() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setStatus(VehicleStatus.RENTED);

        UpdateVehicleRequest request =
                new UpdateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.of(vehicle));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> vehicleService.updateVehicle(vehicleId, request)
                );

        assertEquals(
                ErrorCode.VEHICLE_NOT_EDITABLE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Soft delete vehicle successfully")
    void softDeleteVehicle_ShouldMarkDeleted() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.of(vehicle));

        vehicleService.softDeleteVehicle(vehicleId);

        assertTrue(vehicle.isDeleted());
        assertNotNull(vehicle.getDeletedAt());

        verify(vehicleRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Throw exception when deleting rented vehicle")
    void softDeleteVehicle_ShouldThrow_WhenVehicleRented() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(VehicleStatus.RENTED);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.of(vehicle));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> vehicleService.softDeleteVehicle(vehicleId)
                );

        assertEquals(
                ErrorCode.VEHICLE_NOT_DELETABLE,
                ex.getErrorCode()
        );
    }

    @DisplayName("Should return owner vehicle detail")
    @Test
    void getOwnerVehicleDetail_ShouldReturnVehicle() {

        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();

        VehicleResponse response = mock(VehicleResponse.class);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(
                vehicleId,
                ownerId
        )).thenReturn(Optional.of(vehicle));

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        VehicleResponse result =
                vehicleService.getOwnerVehicleDetail(vehicleId);

        assertNotNull(result);

        verify(vehicleMapper)
                .toResponse(vehicle);
    }

    @DisplayName("Should return available vehicles page")
    @Test
    void getAvailableVehicles_ShouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Vehicle vehicle = new Vehicle();

        Page<Vehicle> vehiclePage =
                new PageImpl<>(List.of(vehicle));

        VehicleResponse response =
                mock(VehicleResponse.class);

        when(vehicleRepository.findByStatusAndDeletedFalse(
                VehicleStatus.AVAILABLE,
                pageable
        )).thenReturn(vehiclePage);

        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(response);

        Page<VehicleResponse> result =
                vehicleService.getAvailableVehicles(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @DisplayName("Should throw exception when license plate is null")
    @Test
    void createVehicle_ShouldThrowException_WhenLicensePlateNull() {

        CreateVehicleRequest request =
                new CreateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate(null);

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.createVehicle(request)
        );

        assertEquals(
                ErrorCode.INVALID_LICENSE_PLATE,
                exception.getErrorCode()
        );

        verify(vehicleRepository, never())
                .save(any());
    }

    @DisplayName("Should throw exception when license plate is blank")
    @Test
    void createVehicle_ShouldThrowException_WhenLicensePlateBlank() {

        CreateVehicleRequest request =
                new CreateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("   ");

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.createVehicle(request)
        );

        assertEquals(
                ErrorCode.INVALID_LICENSE_PLATE,
                exception.getErrorCode()
        );

        verify(vehicleRepository, never())
                .save(any());
    }

    @DisplayName("Should throw exception when update vehicle does not belong to owner")
    @Test
    void updateVehicle_ShouldThrowException_WhenVehicleNotOwnedByOwner() {

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

        UpdateVehicleRequest request =
                new UpdateVehicleRequest();

        request.setYear(2024);
        request.setLicensePlate("72D99999");

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleService.updateVehicle(
                        vehicleId,
                        request
                )
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @DisplayName("Should throw exception when deleting vehicle not found")
    @Test
    void softDeleteVehicle_ShouldThrowException_WhenVehicleNotFound() {

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
}