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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final OwnerService ownerService;

    // =========================
    // CREATE
    // =========================
    @Transactional
    @Override
    public VehicleResponse createVehicle(CreateVehicleRequest request) {

        request.setLicensePlate(
                normalizeLicensePlate(
                        request.getLicensePlate()
                )
        );

        validateCreateVehicle(request);

        VehicleOwner owner = ownerService.getCurrentOwner();


        Vehicle vehicle = vehicleMapper.toEntity(request);

        vehicle.setVehicleOwner(owner);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(vehicle);
    }

    // =========================
    // CUSTOMER: list available vehicles
    // =========================
    @Override
    public Page<VehicleResponse> getAvailableVehicles(Pageable pageable) {

        return vehicleRepository
                .findByStatusAndDeletedFalse(
                        VehicleStatus.AVAILABLE,
                        pageable
                )
                .map(vehicleMapper::toResponse);
    }

    // =========================
    // CUSTOMER: view detail
    // =========================
    @Override
    public VehicleResponse getVehicleDetail(UUID vehicleId) {

        Vehicle vehicle = vehicleRepository
                .findByIdAndStatusAndDeletedFalse(vehicleId, VehicleStatus.AVAILABLE)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        return vehicleMapper.toResponse(vehicle);
    }

    // =========================
    // OWNER: list own vehicles
    // =========================
    @Override
    public Page<VehicleResponse> getOwnerVehicles(Pageable pageable) {

        UUID ownerId = ownerService.getCurrentOwner()
                                   .getId();

        return vehicleRepository
                .findByVehicleOwnerIdAndDeletedFalse(ownerId, pageable)
                .map(vehicleMapper::toResponse);
    }

    // =========================
    // OWNER: view owner's vehicle detail
    // =========================
    @Override
    public VehicleResponse getOwnerVehicleDetail(UUID vehicleId) {

        Vehicle vehicle = getOwnedVehicle(vehicleId);

        return vehicleMapper.toResponse(vehicle);
    }


    // =========================
    // UPDATE (OWNER ONLY)
    // =========================
    @Transactional
    @Override
    public VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request) {

        request.setLicensePlate(
                normalizeLicensePlate(
                        request.getLicensePlate()
                )
        );

        Vehicle vehicle = getOwnedVehicle(vehicleId);


        validateUpdateVehicle(vehicle, request);

        vehicleMapper.updateVehicle(
                request,
                vehicle
        );

        return vehicleMapper.toResponse(vehicle);
    }

    // =========================
    // DELETE (SOFT DELETE)
    // =========================
    @Transactional
    @Override
    public void softDeleteVehicle(UUID vehicleId) {

        Vehicle vehicle = getOwnedVehicle(vehicleId);

        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new AppException(
                    ErrorCode.VEHICLE_NOT_DELETABLE
            );
        }

        vehicle.markDeleted();

    }

    // =========================
    // OWNER VALIDATION
    // =========================
    private Vehicle getOwnedVehicle(UUID vehicleId) {

        UUID ownerId = ownerService.getCurrentOwner()
                                   .getId();

        return vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(vehicleId, ownerId)
                                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    // =========================
    // VALIDATION
    // =========================

    // =========================
    // VALIDATE YEAR OF MANUFACTURE
    // =========================
    private void validateYearOfManufacture(
            Integer year
    ) {
        int currentYear = Year.now()
                              .getValue();

        if (year < 1990
            || year > currentYear + 1) {

            throw new AppException(
                    ErrorCode.INVALID_VEHICLE_YEAR
            );
        }
    }

    // =========================
    // VALIDATE CREATE VEHICLE INPUT
    // =========================
    private void validateCreateVehicle(CreateVehicleRequest request) {

        if (request.getLicensePlate() == null || request.getLicensePlate()
                                                        .isBlank()) {
            throw new AppException(ErrorCode.INVALID_LICENSE_PLATE);
        }

        validateYearOfManufacture(request.getYear());

        validateLicensePlateForCreate(request.getLicensePlate());

    }

    // =========================
    // VALIDATE UPDATE VEHICLE INPUT
    // =========================
    private void validateUpdateVehicle(Vehicle vehicle, UpdateVehicleRequest request) {

        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new AppException(ErrorCode.VEHICLE_NOT_EDITABLE);
        }


        validateYearOfManufacture(request.getYear());

        validateLicensePlateForUpdate(request.getLicensePlate(), vehicle.getId());
    }

    // =========================
    // VALIDATE LICENSE PLATE FOR CREATE
    // =========================
    private void validateLicensePlateForCreate(String licensePlate) {

        if (vehicleRepository.existsByLicensePlateAndDeletedFalse(licensePlate)) {
            throw new AppException(ErrorCode.LICENSE_PLATE_ALREADY_EXISTS);
        }
    }

    // =========================
    // VALIDATE LICENSE PLATE FOR UPDATE
    // =========================
    private void validateLicensePlateForUpdate(String licensePlate, UUID vehicleId) {

        if (vehicleRepository.existsByLicensePlateAndIdNotAndDeletedFalse(licensePlate, vehicleId)) {
            throw new AppException(ErrorCode.LICENSE_PLATE_ALREADY_EXISTS);
        }


    }

    // =========================
    // NORMALIZE LICENSE PLATE
    // =========================
    private String normalizeLicensePlate(String licensePlate) {

        if (licensePlate == null) {
            return null;
        }

        return licensePlate
                .trim()
                .toUpperCase();
    }


}
