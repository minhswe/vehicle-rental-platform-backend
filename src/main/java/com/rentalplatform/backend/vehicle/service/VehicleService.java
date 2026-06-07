package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.vehicle.dto.request.CreateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.request.UpdateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {

    VehicleResponse createVehicle(CreateVehicleRequest request);

    Page<VehicleResponse> getAvailableVehicles(Pageable pageable);

    VehicleResponse getVehicleDetail(UUID vehicleId);

    Page<VehicleResponse> getOwnerVehicles(Pageable pageable);

    VehicleResponse getOwnerVehicleDetail(
            UUID vehicleId
    );


    VehicleResponse updateVehicle(
            UUID vehicleId,
            UpdateVehicleRequest request
    );

    void softDeleteVehicle(UUID vehicleId);
}
