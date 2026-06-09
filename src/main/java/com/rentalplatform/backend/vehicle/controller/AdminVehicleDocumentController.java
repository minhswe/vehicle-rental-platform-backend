package com.rentalplatform.backend.vehicle.controller;

import com.rentalplatform.backend.common.response.ApiResponse;
import com.rentalplatform.backend.vehicle.dto.response.VehicleDocumentResponse;
import com.rentalplatform.backend.vehicle.service.VehicleDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/vehicle-documents")
public class AdminVehicleDocumentController {

    private final VehicleDocumentService vehicleDocumentService;

    @GetMapping("/vehicle/{vehicleId}")
    public ApiResponse<List<VehicleDocumentResponse>>
    getAdminVehicleDocuments(
            @PathVariable UUID vehicleId
    ) {

        return ApiResponse.success(
                vehicleDocumentService
                        .getAdminVehicleDocuments(vehicleId)
        );
    }

    @GetMapping("/pending")
    public ApiResponse<Page<VehicleDocumentResponse>>
    getPendingDocuments(
            Pageable pageable
    ) {

        return ApiResponse.success(
                vehicleDocumentService
                        .getPendingDocuments(pageable)
        );
    }

    // =========================
    // APPROVE DOCUMENT
    // =========================
    @PatchMapping("/{documentId}/approve")
    public ApiResponse<Void> approveDocument(
            @PathVariable UUID documentId
    ) {

        vehicleDocumentService.approveDocument(documentId);

        return ApiResponse.success(null, "Document approved successfully");
    }

    // =========================
    // REJECT DOCUMENT
    // =========================
    @PatchMapping("/{documentId}/reject")
    public ApiResponse<Void> rejectDocument(
            @PathVariable UUID documentId
    ) {

        vehicleDocumentService.rejectDocument(documentId);

        return ApiResponse.success(null, "Document rejected successfully");
    }
}
