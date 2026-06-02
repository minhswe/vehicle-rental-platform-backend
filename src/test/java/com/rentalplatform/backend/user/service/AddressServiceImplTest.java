package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.user.dto.request.CreateAddressRequest;
import com.rentalplatform.backend.user.dto.request.UpdateAddressRequest;
import com.rentalplatform.backend.user.dto.response.AddressResponse;
import com.rentalplatform.backend.user.entity.Address;
import com.rentalplatform.backend.user.mapper.AddressMapper;
import com.rentalplatform.backend.user.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {
    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

    private Address createAddress() {
        Address address = new Address();
        address.setId(addressId);
        address.setUserId(userId);
        address.setDefault(false);
        return address;
    }

    private AddressResponse createResponse() {
        AddressResponse response = new AddressResponse();
        response.setId(addressId);
        return response;
    }

    // ====================================================
    // CREATE
    // ====================================================

    @Test
    @DisplayName("Should create first address as default")
    void shouldCreateFirstAddressAsDefault() {

        CreateAddressRequest request = new CreateAddressRequest();

        Address address = createAddress();
        AddressResponse response = createResponse();

        when(addressMapper.toEntity(request))
                .thenReturn(address);

        when(addressRepository.countByUserId(userId))
                .thenReturn(0L);

        when(addressRepository.save(address))
                .thenReturn(address);

        when(addressMapper.toResponse(address))
                .thenReturn(response);

        AddressResponse result =
                addressService.create(userId, request);

        assertNotNull(result);
        assertTrue(address.isDefault());

        verify(addressRepository)
                .save(address);
    }

    @Test
    @DisplayName("Should create address and set as default")
    void shouldCreateAddressAndSetDefault() {

        CreateAddressRequest request = new CreateAddressRequest();
        request.setDefault(true);

        Address address = createAddress();

        when(addressMapper.toEntity(request))
                .thenReturn(address);

        when(addressRepository.countByUserId(userId))
                .thenReturn(3L);

        when(addressRepository.save(address))
                .thenReturn(address);

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        addressService.create(userId, request);

        verify(addressRepository)
                .clearDefaultAddresses(userId);

        assertTrue(address.isDefault());
    }

    // ====================================================
    // GET ALL
    // ====================================================

    @Test
    @DisplayName("Should get all addresses")
    void shouldGetAllAddresses() {

        Address address = createAddress();

        when(addressRepository.findByUserIdOrderByIsDefaultDesc(userId))
                .thenReturn(List.of(address));

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        List<AddressResponse> result =
                addressService.getAll(userId);

        assertEquals(1, result.size());
    }

    // ====================================================
    // GET BY ID
    // ====================================================

    @Test
    @DisplayName("Should get address by id")
    void shouldGetAddressById() {

        Address address = createAddress();

        when(addressRepository.findByIdAndUserId(userId, addressId))
                .thenReturn(Optional.of(address));

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        AddressResponse result =
                addressService.getById(userId, addressId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw when address not found")
    void shouldThrowWhenAddressNotFound() {

        when(addressRepository.findByIdAndUserId(userId, addressId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> addressService.getById(userId, addressId)
        );
    }

    // ====================================================
    // UPDATE
    // ====================================================

    @Test
    @DisplayName("Should update address")
    void shouldUpdateAddress() {

        Address address = createAddress();

        UpdateAddressRequest request =
                new UpdateAddressRequest();

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(address))
                .thenReturn(address);

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        AddressResponse result =
                addressService.update(
                        userId,
                        addressId,
                        request
                );

        assertNotNull(result);

        verify(addressMapper)
                .update(address, request);

        verify(addressRepository)
                .save(address);
    }

    @Test
    @DisplayName("Should update and set default address")
    void shouldUpdateAndSetDefaultAddress() {

        Address address = createAddress();

        UpdateAddressRequest request =
                new UpdateAddressRequest();

        request.setDefault(true);

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(address))
                .thenReturn(address);

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        addressService.update(
                userId,
                addressId,
                request
        );

        verify(addressRepository)
                .clearDefaultAddresses(userId);

        assertTrue(address.isDefault());
    }

    // ====================================================
    // DELETE
    // ====================================================

    @Test
    @DisplayName("Should delete address")
    void shouldDeleteAddress() {

        Address address = createAddress();

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(address));

        addressService.delete(userId, addressId);

        verify(addressRepository)
                .delete(address);
    }

    @Test
    @DisplayName("Should assign new default after deleting default address")
    void shouldAssignNewDefaultAfterDeletingDefaultAddress() {

        Address deletedAddress = createAddress();
        deletedAddress.setDefault(true);

        Address newDefault = new Address();

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(deletedAddress));

        when(addressRepository.findFirstByUserIdOrderByIdAsc(userId))
                .thenReturn(Optional.of(newDefault));

        addressService.delete(userId, addressId);

        assertTrue(newDefault.isDefault());

        verify(addressRepository)
                .save(newDefault);
    }

    // ====================================================
    // SET DEFAULT
    // ====================================================

    @Test
    @DisplayName("Should set default address")
    void shouldSetDefaultAddress() {

        Address address = createAddress();

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(address))
                .thenReturn(address);

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        AddressResponse result =
                addressService.setDefault(
                        userId,
                        addressId
                );

        assertNotNull(result);
        assertTrue(address.isDefault());

        verify(addressRepository)
                .clearDefaultAddresses(userId);
    }

    // ====================================================
    // GET DEFAULT
    // ====================================================

    @Test
    @DisplayName("Should get default address")
    void shouldGetDefaultAddress() {

        Address address = createAddress();

        when(addressRepository.findByUserIdAndIsDefaultTrue(userId))
                .thenReturn(Optional.of(address));

        when(addressMapper.toResponse(address))
                .thenReturn(createResponse());

        AddressResponse result =
                addressService.getDefault(userId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw when default address not found")
    void shouldThrowWhenDefaultAddressNotFound() {

        when(addressRepository.findByUserIdAndIsDefaultTrue(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> addressService.getDefault(userId)
        );
    }
}
