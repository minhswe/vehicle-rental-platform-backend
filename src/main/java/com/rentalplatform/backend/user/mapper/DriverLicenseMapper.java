package com.rentalplatform.backend.user.mapper;

import com.rentalplatform.backend.user.dto.response.DriverLicenseResponse;
import com.rentalplatform.backend.user.entity.DriverLicense;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverLicenseMapper {
    DriverLicenseResponse toResponse(
            DriverLicense driverLicense
    );
}
