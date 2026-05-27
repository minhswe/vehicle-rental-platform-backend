package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.user.dto.request.UpdateDriverLicenseRequest;
import com.rentalplatform.backend.user.dto.response.DriverLicenseResponse;
import com.rentalplatform.backend.user.entity.DriverLicense;
import com.rentalplatform.backend.user.enums.LicenseVerificationStatus;
import com.rentalplatform.backend.user.mapper.DriverLicenseMapper;
import com.rentalplatform.backend.user.repository.DriverLicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverLicenseServiceImpl implements DriverLicenseService {

    private final DriverLicenseRepository driverLicenseRepository;
    private final DriverLicenseMapper driverLicenseMapper;
    private final StorageService storageService;

    @Override
    public DriverLicenseResponse upload(UUID userId, String licenseNumber, LocalDate expiryDate, MultipartFile frontImage,
                                        MultipartFile backImage) {

        if (driverLicenseRepository.findByUserId(userId)
                                   .isPresent()) {
            throw new AppException(ErrorCode.DRIVER_LICENSE_ALREADY_EXISTS);
        }

        if (driverLicenseRepository.existsByLicenseNumber(licenseNumber)) {
            throw new AppException(ErrorCode.DRIVER_LICENSE_ALREADY_EXISTS);
        }

        String frontImageUrl =
                storageService.upload(
                        frontImage,
                        "users/" + userId + "/driver-licenses/front"
                );

        String backImageUrl =
                storageService.upload(
                        backImage,
                        "users/" + userId + "/driver-licenses/back"
                );

        DriverLicense driverLicense = new DriverLicense();

        driverLicense.setUserId(userId);
        driverLicense.setLicenseNumber(licenseNumber);
        driverLicense.setFrontImageUrl(frontImageUrl);
        driverLicense.setBackImageUrl(backImageUrl);

        driverLicense.setExpiryDate(expiryDate);

        driverLicense.setVerificationStatus(
                LicenseVerificationStatus.PENDING
        );

        driverLicense = driverLicenseRepository.save(driverLicense);

        return driverLicenseMapper.toResponse(driverLicense);
    }

    @Override
    public DriverLicenseResponse getMyLicense(UUID userId) {
        DriverLicense driverLicense = findLicense(userId);

        return driverLicenseMapper.toResponse(driverLicense);
    }

    @Override
    public DriverLicenseResponse update(UUID userId, UpdateDriverLicenseRequest request) {
        DriverLicense driverLicense = findLicense(userId);

        if (request.getLicenseNumber() != null) {

            boolean exists =
                    driverLicenseRepository.existsByLicenseNumber(
                            request.getLicenseNumber()
                    );

            if (exists &&
                !request.getLicenseNumber()
                        .equals(driverLicense.getLicenseNumber())) {

                throw new AppException(
                        ErrorCode.DRIVER_LICENSE_ALREADY_EXISTS
                );
            }

            driverLicense.setLicenseNumber(
                    request.getLicenseNumber()
            );
        }

        if (request.getExpiryDate() != null) {

            driverLicense.setExpiryDate(
                    request.getExpiryDate()
                           .atZone(ZoneOffset.UTC)
                           .toLocalDate()
            );
        }

        // Update status back to pending
        driverLicense.setVerificationStatus(
                LicenseVerificationStatus.PENDING
        );

        driverLicense.setRejectedReason(null);
        driverLicense.setVerifiedAt(null);
        driverLicense.setVerifiedBy(null);

        driverLicense = driverLicenseRepository.save(driverLicense);

        return driverLicenseMapper.toResponse(driverLicense);
    }

    @Override
    public void delete(UUID userId) {
        DriverLicense driverLicense = findLicense(userId);

        storageService.delete(driverLicense.getFrontImageUrl());
        storageService.delete(driverLicense.getBackImageUrl());

        driverLicenseRepository.delete(driverLicense);
    }

    @Override
    public DriverLicenseResponse resubmit(UUID userId) {
        DriverLicense driverLicense = findLicense(userId);

        if (driverLicense.getVerificationStatus()
            != LicenseVerificationStatus.REJECTED) {

            throw new AppException(
                    ErrorCode.DRIVER_LICENSE_NOT_REJECTED
            );
        }

        driverLicense.setVerificationStatus(
                LicenseVerificationStatus.PENDING
        );

        driverLicense.setRejectedReason(null);

        driverLicense = driverLicenseRepository.save(driverLicense);

        return driverLicenseMapper.toResponse(driverLicense);
    }

    // ================= PRIVATE METHODS =================

    private DriverLicense findLicense(UUID userId) {

        return driverLicenseRepository.findByUserId(userId)
                                      .orElseThrow(() -> new AppException(ErrorCode.DRIVER_LICENSE_NOT_FOUND));
    }
}
