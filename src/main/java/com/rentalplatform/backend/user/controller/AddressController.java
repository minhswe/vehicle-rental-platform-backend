package com.rentalplatform.backend.user.controller;

import com.rentalplatform.backend.user.dto.request.CreateAddressRequest;
import com.rentalplatform.backend.user.dto.request.UpdateAddressRequest;
import com.rentalplatform.backend.user.dto.response.AddressResponse;
import com.rentalplatform.backend.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/me/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public AddressResponse create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        return addressService.create(userId, request);
    }

    @GetMapping
    public List<AddressResponse> getAll(
            @AuthenticationPrincipal UUID userId
    ) {
        return addressService.getAll(userId);
    }

    @GetMapping("/{id}")
    public AddressResponse getById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        return addressService.getById(userId, id);
    }

    @PatchMapping("/{id}")
    public AddressResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return addressService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        addressService.delete(userId, id);
    }

    @PatchMapping("/{id}/default")
    public AddressResponse setDefault(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        return addressService.setDefault(userId, id);
    }

    @GetMapping("/default")
    public AddressResponse getDefault(
            @AuthenticationPrincipal UUID userId
    ) {
        return addressService.getDefault(userId);
    }
}
