package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.user.dto.request.UpdateDriverLicenseRequest;
import com.rentalplatform.backend.user.dto.response.DriverLicenseResponse;
import com.rentalplatform.backend.user.entity.DriverLicense;
import com.rentalplatform.backend.user.enums.LicenseVerificationStatus;
import com.rentalplatform.backend.user.mapper.DriverLicenseMapper;
import com.rentalplatform.backend.user.repository.DriverLicenseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriverLicenseServiceImplTest {
    @Mock
    private DriverLicenseRepository driverLicenseRepository;

    @Mock
    private DriverLicenseMapper driverLicenseMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile frontImage;

    @Mock
    private MultipartFile backImage;

    @InjectMocks
    private DriverLicenseServiceImpl service;

    private final UUID userId = UUID.randomUUID();
    private final UUID verifierId = UUID.randomUUID();

    private DriverLicense createLicense() {
        DriverLicense license = new DriverLicense();

        license.setUserId(userId);
        license.setLicenseNumber("A123456");
        license.setFrontImageUrl("front-url");
        license.setBackImageUrl("back-url");
        license.setExpiryDate(LocalDate.now().plusYears(5));
        license.setLicenseVerificationStatus(
                LicenseVerificationStatus.PENDING
        );

        return license;
    }

    private DriverLicenseResponse createResponse() {
        DriverLicenseResponse response =
                new DriverLicenseResponse();

        response.setLicenseNumber("A123456");

        return response;
    }

    @Test
    @DisplayName("Should upload license successfully")
    void shouldUploadSuccessfully() {

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(driverLicenseRepository.existsByLicenseNumber("A123456"))
                .thenReturn(false);

        when(storageService.upload(
                frontImage,
                "users/" + userId + "/driver-licenses/front"
        )).thenReturn("front-url");

        when(storageService.upload(
                backImage,
                "users/" + userId + "/driver-licenses/back"
        )).thenReturn("back-url");

        DriverLicense saved = createLicense();

        when(driverLicenseRepository.save(any(DriverLicense.class)))
                .thenReturn(saved);

        DriverLicenseResponse response =
                createResponse();

        when(driverLicenseMapper.toResponse(saved))
                .thenReturn(response);

        DriverLicenseResponse result =
                service.upload(
                        userId,
                        "A123456",
                        LocalDate.now().plusYears(5),
                        frontImage,
                        backImage
                );

        assertNotNull(result);

        verify(driverLicenseRepository)
                .save(any(DriverLicense.class));
    }

    @Test
    @DisplayName("Should throw when user already has license")
    void shouldThrowWhenUserAlreadyHasLicense() {

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(createLicense()));

        assertThrows(
                AppException.class,
                () -> service.upload(
                        userId,
                        "A123456",
                        LocalDate.now(),
                        frontImage,
                        backImage
                )
        );
    }

    @Test
    @DisplayName("Should throw when license number already exists")
    void shouldThrowWhenLicenseNumberExists() {

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(driverLicenseRepository.existsByLicenseNumber("A123456"))
                .thenReturn(true);

        assertThrows(
                AppException.class,
                () -> service.upload(
                        userId,
                        "A123456",
                        LocalDate.now(),
                        frontImage,
                        backImage
                )
        );
    }

    @Test
    @DisplayName("Should get license successfully")
    void shouldGetLicenseSuccessfully() {

        DriverLicense license = createLicense();

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(license));

        DriverLicenseResponse response =
                createResponse();

        when(driverLicenseMapper.toResponse(license))
                .thenReturn(response);

        DriverLicenseResponse result =
                service.getMyLicense(userId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw when license not found")
    void shouldThrowWhenLicenseNotFound() {

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> service.getMyLicense(userId)
        );
    }

    @Test
    @DisplayName("Should update license successfully")
    void shouldUpdateSuccessfully() {

        DriverLicense license = createLicense();

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(license));

        when(driverLicenseRepository.existsByLicenseNumber("NEW123"))
                .thenReturn(false);

        when(driverLicenseRepository.save(any(DriverLicense.class)))
                .thenReturn(license);

        DriverLicenseResponse response =
                createResponse();

        when(driverLicenseMapper.toResponse(any()))
                .thenReturn(response);

        UpdateDriverLicenseRequest request =
                new UpdateDriverLicenseRequest();

        request.setLicenseNumber("NEW123");

        service.update(userId, request);

        assertEquals(
                "NEW123",
                license.getLicenseNumber()
        );

        assertEquals(
                LicenseVerificationStatus.PENDING,
                license.getLicenseVerificationStatus()
        );
    }

    @Test
    @DisplayName("Should throw when updating to existing license number")
    void shouldThrowWhenUpdatingDuplicateLicenseNumber() {

        DriverLicense license = createLicense();

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(license));

        when(driverLicenseRepository.existsByLicenseNumber("OTHER123"))
                .thenReturn(true);

        UpdateDriverLicenseRequest request =
                new UpdateDriverLicenseRequest();

        request.setLicenseNumber("OTHER123");

        assertThrows(
                AppException.class,
                () -> service.update(userId, request)
        );
    }

    @Test
    @DisplayName("Should delete license successfully")
    void shouldDeleteSuccessfully() {

        DriverLicense license = createLicense();

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(license));

        service.delete(userId);

        verify(storageService)
                .delete("front-url");

        verify(storageService)
                .delete("back-url");

        verify(driverLicenseRepository)
                .delete(license);
    }

    @Test
    @DisplayName("Should resubmit successfully")
    void shouldResubmitSuccessfully() {

        DriverLicense license = createLicense();

        license.setLicenseVerificationStatus(
                LicenseVerificationStatus.REJECTED
        );

        license.setRejectedReason("blur image");

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(license));

        when(driverLicenseRepository.save(any()))
                .thenReturn(license);

        when(driverLicenseMapper.toResponse(any()))
                .thenReturn(createResponse());

        service.resubmit(userId);

        assertEquals(
                LicenseVerificationStatus.PENDING,
                license.getLicenseVerificationStatus()
        );

        assertNull(
                license.getRejectedReason()
        );
    }

    @Test
    @DisplayName("Should throw when license is not rejected")
    void shouldThrowWhenLicenseNotRejected() {

        DriverLicense license = createLicense();

        license.setLicenseVerificationStatus(
                LicenseVerificationStatus.PENDING
        );

        when(driverLicenseRepository.findByUserId(userId))
                .thenReturn(Optional.of(license));

        assertThrows(
                AppException.class,
                () -> service.resubmit(userId)
        );
    }
}
