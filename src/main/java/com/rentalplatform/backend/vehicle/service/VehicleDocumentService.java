package com.rentalplatform.backend.vehicle.service;

import com.rentalplatform.backend.vehicle.dto.response.VehicleDocumentResponse;
import com.rentalplatform.backend.vehicle.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface VehicleDocumentService {

    //=====================================
    // VEHICLE OWNER
    //=====================================

    VehicleDocumentResponse uploadDocument(UUID vehicleId, DocumentType documentType, MultipartFile file);

    List<VehicleDocumentResponse> getOwnerVehicleDocuments(UUID vehicleId);

    void deleteDocument(UUID documentId);

    //=====================================
    // ADMIN
    //=====================================

    List<VehicleDocumentResponse> getAdminVehicleDocuments(UUID vehicleId);

    Page<VehicleDocumentResponse> getPendingDocuments(Pageable pageable);

    void approveDocument(UUID documentId);

    void rejectDocument(UUID documentId);
}
