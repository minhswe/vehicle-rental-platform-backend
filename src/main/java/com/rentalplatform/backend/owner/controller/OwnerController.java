package com.rentalplatform.backend.owner.controller;

import com.rentalplatform.backend.common.constant.ApiPaths;
import com.rentalplatform.backend.common.response.ApiResponse;
import com.rentalplatform.backend.owner.dto.request.RegisterOwnerRequest;
import com.rentalplatform.backend.owner.dto.response.OwnerResponse;
import com.rentalplatform.backend.owner.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/owners")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OwnerResponse>>
    registerOwner(
            @Valid
            @RequestBody
            RegisterOwnerRequest request) {

        OwnerResponse response =
                ownerService.registerOwner(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ApiResponse.success(response));
    }
}
