package com.rentalplatform.backend.vehicle.mapper;

import com.rentalplatform.backend.vehicle.dto.request.CreateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.request.UpdateVehicleRequest;
import com.rentalplatform.backend.vehicle.dto.response.VehicleResponse;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleOwner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Vehicle toEntity(CreateVehicleRequest request);

    @Mapping(
            target = "ownerId",
            source = "vehicleOwner.id"
    )
    VehicleResponse toResponse(Vehicle vehicle);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleOwner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateVehicle(
            UpdateVehicleRequest request,
            @MappingTarget Vehicle vehicle
    );
}