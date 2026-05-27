package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.user.dto.request.UpdateDriverLicenseRequest;
import com.rentalplatform.backend.user.dto.response.DriverLicenseResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;


public interface DriverLicenseService {
    DriverLicenseResponse upload(
            UUID userId,
            String licenseNumber,
            LocalDate expiryDate,
            MultipartFile frontImage,
            MultipartFile backImage
    );

    DriverLicenseResponse getMyLicense(UUID userId);

    DriverLicenseResponse update(
            UUID userId,
            UpdateDriverLicenseRequest request
    );

    void delete(UUID userId);

    DriverLicenseResponse resubmit(UUID userId);
}
