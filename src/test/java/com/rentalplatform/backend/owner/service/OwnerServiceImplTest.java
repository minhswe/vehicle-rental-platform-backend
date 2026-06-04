package com.rentalplatform.backend.owner.service;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.owner.dto.request.RegisterOwnerRequest;
import com.rentalplatform.backend.owner.dto.response.OwnerResponse;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.mapper.OwnerMapper;
import com.rentalplatform.backend.owner.repository.OwnerRepository;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private OwnerMapper ownerMapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private OwnerServiceImpl ownerService;

    private UUID userId;
    private User user;
    private VehicleOwner owner;
    private RegisterOwnerRequest request;
    private OwnerResponse response;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        request = new RegisterOwnerRequest();
        request.setBusinessName("Minh Car Rental");
        request.setDescription("Car rental service");

        owner = new VehicleOwner();
        owner.setUser(user);
        owner.setBusinessName(request.getBusinessName());
        owner.setDescription(request.getDescription());

        response = OwnerResponse.builder()
                                .id(UUID.randomUUID())
                                .userId(userId)
                                .businessName(request.getBusinessName())
                                .description(request.getDescription())
                                .build();
    }

    @Test
    void registerOwner_success() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(ownerRepository.existsByUserId(userId))
                .thenReturn(false);

        when(ownerMapper.toResponse(any(VehicleOwner.class)))
                .thenReturn(response);

        OwnerResponse result =
                ownerService.registerOwner(request);

        assertNotNull(result);

        assertEquals(
                response.getBusinessName(),
                result.getBusinessName()
        );

        verify(ownerRepository)
                .save(any(VehicleOwner.class));

        verify(ownerMapper)
                .toResponse(any(VehicleOwner.class));
    }

    @Test
    void registerOwner_userNotFound() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> ownerService.registerOwner(request)
                );

        assertEquals(
                ErrorCode.USER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(ownerRepository, never())
                .save(any());
    }

    @Test
    void registerOwner_alreadyExists() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(ownerRepository.existsByUserId(userId))
                .thenReturn(true);

        assertThrows(
                AppException.class,
                () -> ownerService.registerOwner(request)
        );

        verify(ownerRepository, never())
                .save(any());
    }

    @Test
    void getCurrentOwner_success() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(ownerRepository.findByUserId(userId))
                .thenReturn(Optional.of(owner));

        VehicleOwner result =
                ownerService.getCurrentOwner();

        assertNotNull(result);

        assertEquals(
                owner.getBusinessName(),
                result.getBusinessName()
        );
    }

    @Test
    void getCurrentOwner_notFound() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(ownerRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> ownerService.getCurrentOwner()
        );
    }
}
