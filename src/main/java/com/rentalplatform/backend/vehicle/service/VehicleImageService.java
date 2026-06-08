package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.vehicle.dto.response.VehicleImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface VehicleImageService {

    VehicleImageResponse uploadImage(
            UUID vehicleId,
            MultipartFile file
    );

    List<VehicleImageResponse> getVehicleImages(
            UUID vehicleId
    );

    void deleteImage(
            UUID imageId
    );
}
