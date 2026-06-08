package com.rentalplatform.backend.vehicle.mapper;

import com.rentalplatform.backend.vehicle.dto.response.VehicleImageResponse;
import com.rentalplatform.backend.vehicle.entity.VehicleImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleImageMapper {

    VehicleImageResponse toResponse(
            VehicleImage vehicleImage
    );
}
