package com.rentalplatform.backend.vehicle.controller;

import com.rentalplatform.backend.common.response.ApiResponse;
import com.rentalplatform.backend.vehicle.dto.response.VehicleImageResponse;
import com.rentalplatform.backend.vehicle.service.VehicleImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleImageController {
    private final VehicleImageService vehicleImageService;

    @PostMapping(
            value = "/{vehicleId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<VehicleImageResponse> uploadImage(
            @PathVariable UUID vehicleId,
            @RequestParam MultipartFile file
    ) {

        return ApiResponse.success(
                vehicleImageService.uploadImage(
                        vehicleId,
                        file
                )
        );
    }

    @GetMapping("/{vehicleId}/images")
    public ApiResponse<List<VehicleImageResponse>>
    getVehicleImages(
            @PathVariable UUID vehicleId
    ) {

        return ApiResponse.success(
                vehicleImageService.getVehicleImages(
                        vehicleId
                )
        );
    }

    @DeleteMapping("/images/{imageId}")
    public ApiResponse<Void> deleteImage(
            @PathVariable UUID imageId
    ) {

        vehicleImageService.deleteImage(
                imageId
        );

        return ApiResponse.success(null);
    }
}
