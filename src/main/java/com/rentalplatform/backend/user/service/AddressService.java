package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.user.dto.request.CreateAddressRequest;
import com.rentalplatform.backend.user.dto.request.UpdateAddressRequest;
import com.rentalplatform.backend.user.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    AddressResponse create(UUID userId, CreateAddressRequest request);
    List<AddressResponse> getAll(UUID userId);
    AddressResponse getById(UUID userId, UUID addressId);
    AddressResponse update(UUID userId, UUID addressId, UpdateAddressRequest request);
    void delete(UUID userId, UUID addressId);
    AddressResponse setDefault(UUID userId, UUID addressId);
    AddressResponse getDefault(UUID userId);
}
