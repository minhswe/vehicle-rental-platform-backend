package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.user.dto.request.CreateAddressRequest;
import com.rentalplatform.backend.user.dto.request.UpdateAddressRequest;
import com.rentalplatform.backend.user.dto.response.AddressResponse;
import com.rentalplatform.backend.user.entity.Address;
import com.rentalplatform.backend.user.mapper.AddressMapper;
import com.rentalplatform.backend.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressResponse create(UUID userId, CreateAddressRequest request) {
        Address address = addressMapper.toEntity(request);
        address.setUserId(userId);

        //if it is the first user's address, set default automatically
        if (addressRepository.countByUserId(userId) == 0) {
            address.setDefault(true);
        }

        //if request want to set as default
        if (request.isDefault()) {
            //clear default address
            addressRepository.clearDefaultAddresses(userId);
            address.setDefault(true);
        }

        address = addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Override
    public List<AddressResponse> getAll(UUID userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                                .stream()
                                .map(addressMapper::toResponse)
                                .toList();
    }

    @Override
    public AddressResponse getById(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(userId, addressId)
                                           .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_OR_USER_NOT_FOUND));

        return addressMapper.toResponse(address);
    }

    @Override
    public AddressResponse update(UUID userId, UUID addressId, UpdateAddressRequest request) {
        Address address = findAddress(userId, addressId);

        addressMapper.update(address, request);

        // Set new default
        if (request.isDefault()) {
            addressRepository.clearDefaultAddresses(userId);
            address.setDefault(true);
        }

        address = addressRepository.save(address);

        return addressMapper.toResponse(address);
    }

    @Override
    public void delete(UUID userId, UUID addressId) {
        Address address = findAddress(userId, addressId);

        boolean wasDefault = address.isDefault();

        addressRepository.delete(address);

        // If deleted default address
        // set another address as default
        if (wasDefault) {

            addressRepository
                    .findFirstByUserIdOrderByIdAsc(userId)
                    .ifPresent(addr -> {
                        addr.setDefault(true);
                        addressRepository.save(addr);
                    });
        }
    }

    @Override
    public AddressResponse setDefault(UUID userId, UUID addressId) {
        Address address = findAddress(userId, addressId);

        addressRepository.clearDefaultAddresses(userId);

        address.setDefault(true);

        address = addressRepository.save(address);

        return addressMapper.toResponse(address);
    }

    @Override
    public AddressResponse getDefault(UUID userId) {
        Address address = addressRepository
                .findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() ->
                                     new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        return addressMapper.toResponse(address);
    }
    // ================= PRIVATE METHODS =================
    private Address findAddress(UUID userId,
                                UUID addressId) {

        return addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                                     new AppException(ErrorCode.ADDRESS_NOT_FOUND));
    }
}
