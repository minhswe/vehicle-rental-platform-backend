package com.rentalplatform.backend.vehicle.controller;

import com.rentalplatform.backend.common.response.ApiResponse;
import com.rentalplatform.backend.vehicle.dto.response.VehicleDocumentResponse;
import com.rentalplatform.backend.vehicle.enums.DocumentType;
import com.rentalplatform.backend.vehicle.service.VehicleDocumentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/vehicles")
public class VehicleDocumentController {

    private final VehicleDocumentService
            vehicleDocumentService;

    @PostMapping(
            value = "/{vehicleId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<VehicleDocumentResponse>
    uploadDocument(
            @PathVariable UUID vehicleId,
            @RequestParam DocumentType documentType,
            @RequestPart MultipartFile file
    ) {

        return ApiResponse.success(
                vehicleDocumentService.uploadDocument(
                        vehicleId,
                        documentType,
                        file
                )
        );
    }

    @GetMapping("/owner/vehicles/{vehicleId}/documents")
    public ApiResponse<List<VehicleDocumentResponse>>
    getOwnerVehicleDocuments(
            @PathVariable UUID vehicleId
    ) {

        return ApiResponse.<List<VehicleDocumentResponse>>builder()
                          .success(true)
                          .data(
                                  vehicleDocumentService.getOwnerVehicleDocuments(vehicleId)
                          )
                          .message("Success")
                          .traceId(MDC.get("traceId"))
                          .timestamp(Instant.now())
                          .build();
    }

    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<Void> deleteDocument(
            @PathVariable UUID documentId
    ) {

        vehicleDocumentService
                .deleteDocument(documentId);

        return ApiResponse.success(
                null,
                "Document deleted successfully"
        );
    }

}
