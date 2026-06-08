package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.service.OwnerService;
import com.rentalplatform.backend.vehicle.dto.response.VehicleImageResponse;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.entity.VehicleImage;
import com.rentalplatform.backend.vehicle.mapper.VehicleImageMapper;
import com.rentalplatform.backend.vehicle.repository.VehicleImageRepository;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleImageServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleImageRepository vehicleImageRepository;

    @Mock
    private VehicleImageMapper vehicleImageMapper;

    @Mock
    private OwnerService ownerService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private VehicleImageServiceImpl vehicleImageService;

    @Test
    @DisplayName("Should upload image successfully")
    void uploadImage_ShouldUploadSuccessfully() {

        UUID vehicleId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();

        VehicleImage image = new VehicleImage();

        VehicleImageResponse response =
                mock(VehicleImageResponse.class);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             ownerId
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleImageRepository.countByVehicleId(vehicleId))
                .thenReturn(0L);

        when(storageService.upload(file, "vehicles"))
                .thenReturn("image-url");

        when(vehicleImageRepository.save(any(VehicleImage.class)))
                .thenReturn(image);

        when(vehicleImageMapper.toResponse(image))
                .thenReturn(response);

        VehicleImageResponse result =
                vehicleImageService.uploadImage(
                        vehicleId,
                        file
                );

        assertNotNull(result);

        verify(storageService)
                .upload(file, "vehicles");

        verify(vehicleImageRepository)
                .save(any(VehicleImage.class));
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void uploadImage_ShouldThrowException_WhenFileEmpty() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        UUID vehicleId = UUID.randomUUID();

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> vehicleImageService.uploadImage(
                                vehicleId,
                                file
                        )
                );
        assertEquals(
                ErrorCode.FILE_EMPTY,
                exception.getErrorCode()
        );

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("Should throw exception when file is too large")
    void uploadImage_ShouldThrowException_WhenFileTooLarge() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(6 * 1024 * 1024L);

        UUID vehicleId = UUID.randomUUID();

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> vehicleImageService.uploadImage(
                                vehicleId,
                                file
                        )
                );

        assertEquals(
                ErrorCode.FILE_TOO_LARGE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when image type is invalid")
    void uploadImage_ShouldThrowException_WhenInvalidImageType() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        UUID vehicleId = UUID.randomUUID();

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> vehicleImageService.uploadImage(
                                vehicleId,
                                file
                        )
                );

        assertEquals(
                ErrorCode.INVALID_IMAGE_TYPE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when vehicle not found")
    void uploadImage_ShouldThrowException_WhenVehicleNotFound() {

        UUID vehicleId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> vehicleImageService.uploadImage(
                                vehicleId,
                                file
                        )
                );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when maximum images exceeded")
    void uploadImage_ShouldThrowException_WhenMaxImagesExceeded() {

        UUID vehicleId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleImageRepository.countByVehicleId(vehicleId))
                .thenReturn(10L);

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> vehicleImageService.uploadImage(
                                vehicleId,
                                file
                        )
                );

        assertEquals(
                ErrorCode.MAX_IMAGES_EXCEEDED,
                exception.getErrorCode()
        );

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("Should return vehicle images")
    void getVehicleImages_ShouldReturnImages() {

        UUID vehicleId = UUID.randomUUID();

        VehicleImage image = new VehicleImage();

        VehicleImageResponse response =
                mock(VehicleImageResponse.class);

        when(vehicleImageRepository
                     .findByVehicleIdOrderBySortOrderAsc(vehicleId))
                .thenReturn(List.of(image));

        when(vehicleImageMapper.toResponse(image))
                .thenReturn(response);

        List<VehicleImageResponse> result =
                vehicleImageService.getVehicleImages(vehicleId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no images exist")
    void getVehicleImages_ShouldReturnEmptyList() {

        UUID vehicleId = UUID.randomUUID();

        when(vehicleImageRepository
                     .findByVehicleIdOrderBySortOrderAsc(vehicleId))
                .thenReturn(List.of());

        List<VehicleImageResponse> result =
                vehicleImageService.getVehicleImages(vehicleId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete image successfully")
    void deleteImage_ShouldDeleteSuccessfully() {

        UUID imageId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        VehicleImage image = new VehicleImage();
        image.setImageUrl("image-url");

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleImageRepository
                     .findByIdAndVehicleVehicleOwnerId(
                             imageId,
                             owner.getId()
                     ))
                .thenReturn(Optional.of(image));

        vehicleImageService.deleteImage(imageId);

        verify(storageService)
                .delete("image-url");

        verify(vehicleImageRepository)
                .delete(image);
    }

    @Test
    @DisplayName("Should throw exception when image not found")
    void deleteImage_ShouldThrowException_WhenImageNotFound() {

        UUID imageId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleImageRepository
                     .findByIdAndVehicleVehicleOwnerId(
                             imageId,
                             owner.getId()
                     ))
                .thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> vehicleImageService.deleteImage(imageId)
                );

        assertEquals(
                ErrorCode.VEHICLE_IMAGE_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(storageService);
    }
}
