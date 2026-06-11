package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.service.OwnerContextService;
import com.rentalplatform.backend.owner.service.OwnerService;
import com.rentalplatform.backend.vehicle.dto.response.VehicleDocumentResponse;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.entity.VehicleDocument;
import com.rentalplatform.backend.vehicle.enums.DocumentType;
import com.rentalplatform.backend.vehicle.enums.VerificationStatus;
import com.rentalplatform.backend.vehicle.mapper.VehicleDocumentMapper;
import com.rentalplatform.backend.vehicle.repository.VehicleDocumentRepository;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleDocumentServiceImpl implements VehicleDocumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private static final int MAX_DOCUMENTS = 10;

    private static final String DOCUMENT_FOLDER = "vehicle-documents";

    private final VehicleRepository vehicleRepository;

    private final VehicleDocumentRepository vehicleDocumentRepository;

    private final VehicleDocumentMapper vehicleDocumentMapper;

    private final OwnerService ownerService;

    private final OwnerContextService ownerContextService;

    private final StorageService storageService;


    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            );
    //=========================================
    // Vehicle Owner Methods
    //=========================================

    @Transactional
    @Override
    public VehicleDocumentResponse uploadDocument(UUID vehicleId, DocumentType documentType, MultipartFile file) {
        String documentUrl = null;

        try {
            if (documentType == null) {
                throw new AppException(
                        ErrorCode.INVALID_DOCUMENT_TYPE
                );
            }

            String contentType = file.getContentType();

            if (contentType == null
                || !ALLOWED_TYPES.contains(contentType)) {
                throw new AppException(
                        ErrorCode.INVALID_DOCUMENT_TYPE
                );
            }

            validateDocument(file);

            if (vehicleDocumentRepository
                    .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                            vehicleId,
                            documentType
                    )) {

                throw new AppException(
                        ErrorCode.DOCUMENT_ALREADY_EXISTS
                );
            }


            UUID ownerId = ownerContextService.getCurrentOwnerId();

            Vehicle vehicle = vehicleRepository.findByIdAndVehicleOwnerIdAndDeletedFalse(vehicleId, ownerId)
                                               .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

            long documentCount = vehicleDocumentRepository.countByVehicleIdAndDeletedFalse(vehicleId);

            if (documentCount >= MAX_DOCUMENTS) {
                throw new AppException(ErrorCode.MAX_DOCUMENTS_EXCEEDED);
            }

            documentUrl = storageService.upload(file, DOCUMENT_FOLDER);

            VehicleDocument document = new VehicleDocument();

            document.setVehicle(vehicle);

            document.setDocumentType(documentType);

            document.setDocumentUrl(documentUrl);

            document.setVerificationStatus(VerificationStatus.PENDING);

            VehicleDocument saved = vehicleDocumentRepository.save(document);

            return vehicleDocumentMapper.toResponse(saved);
        } catch (Exception ex) {
            if (documentUrl != null) {
                storageService.delete(documentUrl);
            }

            throw ex;
        }

    }

    @Override
    public List<VehicleDocumentResponse> getOwnerVehicleDocuments(UUID vehicleId) {

        UUID ownerId = ownerContextService.getCurrentOwnerId();

        vehicleRepository
                .findByIdAndVehicleOwnerIdAndDeletedFalse(
                        vehicleId,
                        ownerId
                )
                .orElseThrow(() ->
                                     new AppException(
                                             ErrorCode.VEHICLE_NOT_FOUND
                                     )
                );

        return vehicleDocumentRepository
                .findByVehicleIdAndDeletedFalse(
                        vehicleId
                )
                .stream()
                .map(vehicleDocumentMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void deleteDocument(UUID documentId) {


        UUID ownerId = ownerContextService.getCurrentOwnerId();

        VehicleDocument document =
                vehicleDocumentRepository.findByIdAndVehicleVehicleOwnerIdAndDeletedFalse(documentId, ownerId)
                                         .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_DOCUMENT_NOT_FOUND));

        if (
                document.getVerificationStatus()
                == VerificationStatus.VERIFIED
        ) {
            throw new AppException(
                    ErrorCode.DOCUMENT_CANNOT_BE_DELETED
            );
        }

        storageService.delete(document.getDocumentUrl());

        document.markDeleted();
    }


    private void validateDocument(MultipartFile file) {

        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    //=========================================
    // Admin Methods
    //=========================================

    @Override
    public List<VehicleDocumentResponse> getAdminVehicleDocuments(
            UUID vehicleId
    ) {

        vehicleRepository
                .findByIdAndDeletedFalse(vehicleId)
                .orElseThrow(() ->
                                     new AppException(ErrorCode.VEHICLE_NOT_FOUND)
                );

        return vehicleDocumentRepository
                .findByVehicleIdAndDeletedFalse(
                        vehicleId
                )
                .stream()
                .map(vehicleDocumentMapper::toResponse)
                .toList();
    }

    @Override
    public Page<VehicleDocumentResponse> getPendingDocuments(
            Pageable pageable
    ) {

        return vehicleDocumentRepository
                .findByVerificationStatusAndDeletedFalse(
                        VerificationStatus.PENDING,
                        pageable
                )
                .map(vehicleDocumentMapper::toResponse);
    }

    @Transactional
    @Override
    public void approveDocument(UUID documentId) {

        VehicleDocument document =
                getPendingDocument(documentId);

        document.setVerificationStatus(
                VerificationStatus.VERIFIED
        );

        document.setVerifiedAt(
                Instant.now()
        );
    }

    @Transactional
    @Override
    public void rejectDocument(UUID documentId) {

        VehicleDocument document =
                getPendingDocument(documentId);

        document.setVerificationStatus(
                VerificationStatus.REJECTED
        );

        document.setVerifiedAt(
                Instant.now()
        );
    }

    private VehicleDocument getPendingDocument(
            UUID documentId
    ) {

        VehicleDocument document =
                vehicleDocumentRepository
                        .findByIdAndDeletedFalse(documentId)
                        .orElseThrow(() ->
                                             new AppException(
                                                     ErrorCode.VEHICLE_DOCUMENT_NOT_FOUND
                                             )
                        );

        if (document.getVerificationStatus()
            != VerificationStatus.PENDING) {

            throw new AppException(
                    ErrorCode.DOCUMENT_ALREADY_PROCESSED
            );
        }

        return document;
    }
}