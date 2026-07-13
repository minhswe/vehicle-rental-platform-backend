package com.rentalplatform.backend.user.controller;

import com.rentalplatform.backend.common.constant.ApiPaths;
import com.rentalplatform.backend.user.dto.request.UpdateDriverLicenseRequest;
import com.rentalplatform.backend.user.dto.response.DriverLicenseResponse;
import com.rentalplatform.backend.user.service.DriverLicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/users/me/driver-license")
@RequiredArgsConstructor
public class DriverLicenseController {
    private final DriverLicenseService driverLicenseService;

    /**
     * Upload driver license
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DriverLicenseResponse upload(
            @AuthenticationPrincipal UUID userId,

            @RequestParam String licenseNumber,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate expiryDate,

            @RequestPart MultipartFile frontImage,

            @RequestPart MultipartFile backImage
    ) {

        return driverLicenseService.upload(
                userId,
                licenseNumber,
                expiryDate,
                frontImage,
                backImage
        );
    }

    /**
     * Get my driver license
     */
    @GetMapping
    public DriverLicenseResponse getMyLicense(
            @AuthenticationPrincipal UUID userId
    ) {

        return driverLicenseService.getMyLicense(userId);
    }

    /**
     * Update driver license info
     */
    @PatchMapping
    public DriverLicenseResponse update(
            @AuthenticationPrincipal UUID userId,

            @Valid
            @RequestBody
            UpdateDriverLicenseRequest request
    ) {

        return driverLicenseService.update(
                userId,
                request
        );
    }

    /**
     * Delete driver license
     */
    @DeleteMapping
    public void delete(
            @AuthenticationPrincipal UUID userId
    ) {

        driverLicenseService.delete(userId);
    }

    /**
     * Resubmit rejected driver license
     */
    @PostMapping("/resubmit")
    public DriverLicenseResponse resubmit(
            @AuthenticationPrincipal UUID userId
    ) {

        return driverLicenseService.resubmit(userId);
    }
}
