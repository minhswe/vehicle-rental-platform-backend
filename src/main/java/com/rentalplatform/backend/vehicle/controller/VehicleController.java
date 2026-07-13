package com.rentalplatform.backend.vehicle.controller;

import com.rentalplatform.backend.common.constant.ApiPaths;
import com.rentalplatform.backend.common.response.ApiResponse;
import com.rentalplatform.backend.vehicle.dto.request.CreateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.request.UpdateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.response.VehicleResponse;
import com.rentalplatform.backend.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // =========================
    // CUSTOMER APIs
    // =========================

    @GetMapping
    public ApiResponse<Page<VehicleResponse>> getAvailableVehicles(
            Pageable pageable
    ) {
        return ApiResponse.success(
                vehicleService.getAvailableVehicles(pageable)
        );
    }

    @GetMapping("/{vehicleId}")
    public ApiResponse<VehicleResponse> getVehicleDetail(
            @PathVariable UUID vehicleId
    ) {
        return ApiResponse.success(
                vehicleService.getVehicleDetail(vehicleId)
        );
    }

    // =========================
    // OWNER APIs
    // =========================

    @PostMapping
    public ApiResponse<VehicleResponse> createVehicle(
            @Valid @RequestBody
            CreateVehicleRequest request
    ) {
        return ApiResponse.success(
                vehicleService.createVehicle(request)
        );
    }

    @GetMapping("/my")
    public ApiResponse<Page<VehicleResponse>> getOwnerVehicles(
            Pageable pageable
    ) {
        return ApiResponse.success(
                vehicleService.getOwnerVehicles(pageable)
        );
    }

    @GetMapping("/my/{vehicleId}")
    public ApiResponse<VehicleResponse> getOwnerVehicleDetail(
            @PathVariable UUID vehicleId
    ) {
        return ApiResponse.success(
                vehicleService.getOwnerVehicleDetail(vehicleId)
        );
    }

    @PutMapping("/{vehicleId}")
    public ApiResponse<VehicleResponse> updateVehicle(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody
            UpdateVehicleRequest request
    ) {
        return ApiResponse.success(
                vehicleService.updateVehicle(vehicleId, request)
        );
    }

    @DeleteMapping("/{vehicleId}")
    public ApiResponse<Void> deleteVehicle(
            @PathVariable UUID vehicleId
    ) {
        vehicleService.softDeleteVehicle(vehicleId);

        return ApiResponse.success(null);
    }
}