package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.owner.service.OwnerContextService;
import com.rentalplatform.backend.vehicle.dto.response.VehicleImageResponse;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.entity.VehicleImage;
import com.rentalplatform.backend.vehicle.mapper.VehicleImageMapper;
import com.rentalplatform.backend.vehicle.repository.VehicleImageRepository;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleImageServiceImpl implements VehicleImageService {

    private final VehicleRepository vehicleRepository;

    private final VehicleImageRepository vehicleImageRepository;

    private final VehicleImageMapper vehicleImageMapper;

    private final OwnerContextService ownerContextService;

    private final StorageService storageService;

    private static final int MAX_VEHICLE_IMAGES = 10;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    @Transactional
    public VehicleImageResponse uploadImage(UUID vehicleId, MultipartFile file) {

        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        UUID ownerId = ownerContextService.getCurrentOwnerId();

        Vehicle vehicle = vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(vehicleId, ownerId)
                                           .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        long imageCount = vehicleImageRepository.countByVehicleId(vehicleId);

        if (imageCount >= MAX_VEHICLE_IMAGES) {
            throw new AppException(ErrorCode.MAX_IMAGES_EXCEEDED);
        }

        String imageUrl = storageService.upload(file, "vehicles");

        VehicleImage image = new VehicleImage();

        image.setVehicle(vehicle);

        image.setImageUrl(imageUrl);

        image.setSortOrder((int) imageCount + 1);

        VehicleImage savedImage = vehicleImageRepository.save(image);

        return vehicleImageMapper.toResponse(savedImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleImageResponse> getVehicleImages(UUID vehicleId) {
        return vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId)
                                     .stream()
                                     .map(vehicleImageMapper::toResponse)
                                     .toList();
    }

    @Override
    @Transactional
    public void deleteImage(UUID imageId) {
        UUID ownerId = ownerContextService.getCurrentOwnerId();

        VehicleImage image = vehicleImageRepository.findByIdAndVehicleVehicleOwnerId(imageId, ownerId)
                                                   .orElseThrow(
                                                           () -> new AppException(ErrorCode.VEHICLE_IMAGE_NOT_FOUND));

        storageService.delete(image.getImageUrl());

        vehicleImageRepository.delete(image);
    }
}

