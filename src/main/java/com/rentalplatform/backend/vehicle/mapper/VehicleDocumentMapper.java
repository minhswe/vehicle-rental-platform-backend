package com.rentalplatform.backend.vehicle.mapper;

import com.rentalplatform.backend.vehicle.dto.response.VehicleDocumentResponse;
import com.rentalplatform.backend.vehicle.entity.VehicleDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleDocumentMapper {

    VehicleDocumentResponse toResponse(
            VehicleDocument document
    );

}
