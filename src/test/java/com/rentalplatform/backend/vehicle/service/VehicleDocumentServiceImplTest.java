package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.service.OwnerService;
import com.rentalplatform.backend.vehicle.dto.response.VehicleDocumentResponse;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.entity.VehicleDocument;
import com.rentalplatform.backend.vehicle.enums.DocumentType;
import com.rentalplatform.backend.vehicle.enums.VerificationStatus;
import com.rentalplatform.backend.vehicle.mapper.VehicleDocumentMapper;
import com.rentalplatform.backend.vehicle.repository.VehicleDocumentRepository;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleDocumentServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleDocumentRepository vehicleDocumentRepository;

    @Mock
    private VehicleDocumentMapper vehicleDocumentMapper;

    @Mock
    private OwnerService ownerService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private VehicleDocumentServiceImpl service;

    @Mock
    private MultipartFile file;

    @Test
    void uploadDocument_ShouldSuccess() {

        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        VehicleDocument document = new VehicleDocument();

        VehicleDocumentResponse response =
                mock(VehicleDocumentResponse.class);

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.isEmpty())
                .thenReturn(false);

        when(file.getSize())
                .thenReturn(1024L);

        when(vehicleDocumentRepository
                     .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                             vehicleId,
                             DocumentType.REGISTRATION
                     ))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .countByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(0L);

        when(storageService.upload(file, "vehicle-documents"))
                .thenReturn("url");

        when(vehicleDocumentRepository.save(any()))
                .thenReturn(document);

        when(vehicleDocumentMapper.toResponse(document))
                .thenReturn(response);

        VehicleDocumentResponse result =
                service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                );

        assertNotNull(result);

        verify(storageService)
                .upload(file, "vehicle-documents");
    }

    @Test
    void uploadDocument_ShouldThrow_WhenDocumentTypeNull() {

        AppException ex = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        UUID.randomUUID(),
                        null,
                        file
                )
        );

        assertEquals(
                ErrorCode.INVALID_DOCUMENT_TYPE,
                ex.getErrorCode()
        );
    }

    @Test
    void uploadDocument_ShouldThrow_WhenFileEmpty() {

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.isEmpty())
                .thenReturn(true);

        AppException ex = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        UUID.randomUUID(),
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.FILE_EMPTY,
                ex.getErrorCode()
        );
    }

    @Test
    void uploadDocument_ShouldDeleteUploadedFile_WhenSaveFails() {

        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.isEmpty())
                .thenReturn(false);

        when(file.getSize())
                .thenReturn(1000L);

        when(vehicleDocumentRepository
                     .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                             any(),
                             any()
                     ))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .countByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(0L);

        when(storageService.upload(any(), any()))
                .thenReturn("uploaded-url");

        when(vehicleDocumentRepository.save(any()))
                .thenThrow(RuntimeException.class);

        assertThrows(
                RuntimeException.class,
                () -> service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                )
        );

        verify(storageService)
                .delete("uploaded-url");
    }

    @Test
    void getOwnerVehicleDocuments_ShouldReturnList() {

        UUID vehicleId = UUID.randomUUID();

        UUID ownerId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        Vehicle vehicle = new Vehicle();

        VehicleDocument document =
                new VehicleDocument();

        VehicleDocumentResponse response =
                mock(VehicleDocumentResponse.class);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             ownerId
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .findByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(List.of(document));

        when(vehicleDocumentMapper.toResponse(document))
                .thenReturn(response);

        List<VehicleDocumentResponse> result =
                service.getOwnerVehicleDocuments(vehicleId);

        assertEquals(1, result.size());
    }

    @Test
    void deleteDocument_ShouldSoftDelete() {

        UUID documentId = UUID.randomUUID();

        UUID ownerId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        VehicleDocument document =
                new VehicleDocument();

        document.setVerificationStatus(
                VerificationStatus.PENDING
        );

        document.setDocumentUrl("url");

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleDocumentRepository
                     .findByIdAndVehicleVehicleOwnerIdAndDeletedFalse(
                             documentId,
                             ownerId
                     ))
                .thenReturn(Optional.of(document));

        service.deleteDocument(documentId);

        assertTrue(document.isDeleted());

        verify(storageService)
                .delete("url");
    }

    @Test
    void deleteDocument_ShouldThrow_WhenVerified() {

        UUID documentId = UUID.randomUUID();

        UUID ownerId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(ownerId);

        VehicleDocument document =
                new VehicleDocument();

        document.setVerificationStatus(
                VerificationStatus.VERIFIED
        );

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleDocumentRepository
                     .findByIdAndVehicleVehicleOwnerIdAndDeletedFalse(
                             documentId,
                             ownerId
                     ))
                .thenReturn(Optional.of(document));

        AppException ex = assertThrows(
                AppException.class,
                () -> service.deleteDocument(documentId)
        );

        assertEquals(
                ErrorCode.DOCUMENT_CANNOT_BE_DELETED,
                ex.getErrorCode()
        );
    }

    @Test
    void getPendingDocuments_ShouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<VehicleDocument> page =
                new PageImpl<>(List.of(new VehicleDocument()));

        when(vehicleDocumentRepository
                     .findByVerificationStatusAndDeletedFalse(
                             VerificationStatus.PENDING,
                             pageable
                     ))
                .thenReturn(page);

        Page<VehicleDocumentResponse> result =
                service.getPendingDocuments(pageable);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception when vehicle does not exist during document upload")
    void uploadDocument_ShouldThrow_WhenVehicleNotFound() {

        UUID vehicleId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        when(vehicleDocumentRepository
                     .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                             vehicleId,
                             DocumentType.REGISTRATION
                     ))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.empty());


        AppException exception = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when document type already exists")
    void uploadDocument_ShouldThrow_WhenDocumentAlreadyExists() {

        UUID vehicleId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        when(vehicleDocumentRepository
                     .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                             vehicleId,
                             DocumentType.REGISTRATION
                     ))
                .thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.DOCUMENT_ALREADY_EXISTS,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when uploaded file exceeds maximum size")
    void uploadDocument_ShouldThrow_WhenFileTooLarge() {

        UUID vehicleId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20L * 1024 * 1024);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.FILE_TOO_LARGE,
                exception.getErrorCode()
        );

        verify(file).getSize();
    }

    @Test
    @DisplayName("Should return owner vehicle documents successfully")
    void getOwnerVehicleDocuments_ShouldReturnDocuments() {

        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        VehicleDocument document = new VehicleDocument();

        VehicleDocumentResponse response =
                mock(VehicleDocumentResponse.class);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .findByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(List.of(document));

        when(vehicleDocumentMapper.toResponse(document))
                .thenReturn(response);

        List<VehicleDocumentResponse> result =
                service.getOwnerVehicleDocuments(vehicleId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw exception when owner tries to access vehicle not owned")
    void getOwnerVehicleDocuments_ShouldThrow_WhenVehicleNotOwned() {

        UUID vehicleId = UUID.randomUUID();

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

        AppException exception = assertThrows(
                AppException.class,
                () -> service.getOwnerVehicleDocuments(vehicleId)
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should return vehicle documents for admin")
    void getAdminVehicleDocuments_ShouldReturnDocuments() {

        UUID vehicleId = UUID.randomUUID();

        Vehicle vehicle = new Vehicle();

        VehicleDocument document = new VehicleDocument();

        VehicleDocumentResponse response =
                mock(VehicleDocumentResponse.class);

        when(vehicleRepository
                     .findByIdAndDeletedFalse(vehicleId))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .findByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(List.of(document));

        when(vehicleDocumentMapper.toResponse(document))
                .thenReturn(response);

        List<VehicleDocumentResponse> result =
                service.getAdminVehicleDocuments(vehicleId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw exception when admin requests documents of non-existing vehicle")
    void getAdminVehicleDocuments_ShouldThrow_WhenVehicleNotFound() {

        UUID vehicleId = UUID.randomUUID();

        when(vehicleRepository
                     .findByIdAndDeletedFalse(vehicleId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> service.getAdminVehicleDocuments(vehicleId)
        );

        assertEquals(
                ErrorCode.VEHICLE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when document does not exist")
    void deleteDocument_ShouldThrow_WhenDocumentNotFound() {

        UUID documentId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleDocumentRepository
                     .findByIdAndVehicleVehicleOwnerIdAndDeletedFalse(
                             documentId,
                             owner.getId()
                     ))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> service.deleteDocument(documentId)
        );

        assertEquals(
                ErrorCode.VEHICLE_DOCUMENT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should delete uploaded file when database save fails")
    void uploadDocument_ShouldDeleteFile_WhenSaveFails() {

        UUID vehicleId = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        when(vehicleDocumentRepository
                     .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                             vehicleId,
                             DocumentType.REGISTRATION
                     ))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()
                     ))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .countByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(0L);

        when(storageService.upload(file, "vehicle-documents"))
                .thenReturn("uploaded-url");

        when(vehicleDocumentRepository.save(any()))
                .thenThrow(new RuntimeException("DB Error"));

        assertThrows(
                RuntimeException.class,
                () -> service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                )
        );

        verify(storageService)
                .delete("uploaded-url");
    }

    @Test
    @DisplayName("Should throw exception when content type is null")
    void uploadDocument_ShouldThrow_WhenContentTypeNull() {

        when(file.getContentType()).thenReturn(null);

        AppException ex = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        UUID.randomUUID(),
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.INVALID_DOCUMENT_TYPE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when file type is invalid")
    void uploadDocument_ShouldThrow_WhenInvalidContentType() {

        when(file.getContentType())
                .thenReturn("application/zip");

        AppException ex = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        UUID.randomUUID(),
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.INVALID_DOCUMENT_TYPE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw exception when maximum document limit exceeded")
    void uploadDocument_ShouldThrow_WhenMaxDocumentsExceeded() {

        UUID vehicleId = UUID.randomUUID();

        VehicleOwner owner = new VehicleOwner();
        owner.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle();

        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        when(vehicleDocumentRepository
                     .existsByVehicleIdAndDocumentTypeAndDeletedFalse(
                             vehicleId,
                             DocumentType.REGISTRATION))
                .thenReturn(false);

        when(ownerService.getCurrentOwner())
                .thenReturn(owner);

        when(vehicleRepository
                     .findByIdAndVehicleOwnerIdAndDeletedFalse(
                             vehicleId,
                             owner.getId()))
                .thenReturn(Optional.of(vehicle));

        when(vehicleDocumentRepository
                     .countByVehicleIdAndDeletedFalse(vehicleId))
                .thenReturn(10L);

        AppException ex = assertThrows(
                AppException.class,
                () -> service.uploadDocument(
                        vehicleId,
                        DocumentType.REGISTRATION,
                        file
                )
        );

        assertEquals(
                ErrorCode.MAX_DOCUMENTS_EXCEEDED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should approve pending document successfully")
    void approveDocument_ShouldSuccess() {

        UUID documentId = UUID.randomUUID();

        VehicleDocument document = new VehicleDocument();

        document.setVerificationStatus(
                VerificationStatus.PENDING
        );

        when(vehicleDocumentRepository
                     .findByIdAndDeletedFalse(documentId))
                .thenReturn(Optional.of(document));

        service.approveDocument(documentId);

        assertEquals(
                VerificationStatus.VERIFIED,
                document.getVerificationStatus()
        );

        assertNotNull(document.getVerifiedAt());
    }

    @Test
    @DisplayName("Should throw exception when approving non-existing document")
    void approveDocument_ShouldThrow_WhenDocumentNotFound() {

        UUID documentId = UUID.randomUUID();

        when(vehicleDocumentRepository
                     .findByIdAndDeletedFalse(documentId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> service.approveDocument(documentId)
        );

        assertEquals(
                ErrorCode.VEHICLE_DOCUMENT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should reject pending document successfully")
    void rejectDocument_ShouldSuccess() {

        UUID documentId = UUID.randomUUID();

        VehicleDocument document = new VehicleDocument();

        document.setVerificationStatus(
                VerificationStatus.PENDING
        );

        when(vehicleDocumentRepository
                     .findByIdAndDeletedFalse(documentId))
                .thenReturn(Optional.of(document));

        service.rejectDocument(documentId);

        assertEquals(
                VerificationStatus.REJECTED,
                document.getVerificationStatus()
        );

        assertNotNull(document.getVerifiedAt());
    }

    @Test
    @DisplayName("Should throw exception when document already processed")
    void rejectDocument_ShouldThrow_WhenAlreadyProcessed() {

        UUID documentId = UUID.randomUUID();

        VehicleDocument document = new VehicleDocument();

        document.setVerificationStatus(
                VerificationStatus.VERIFIED
        );

        when(vehicleDocumentRepository
                     .findByIdAndDeletedFalse(documentId))
                .thenReturn(Optional.of(document));

        AppException exception = assertThrows(
                AppException.class,
                () -> service.rejectDocument(documentId)
        );

        assertEquals(
                ErrorCode.DOCUMENT_ALREADY_PROCESSED,
                exception.getErrorCode()
        );
    }
}