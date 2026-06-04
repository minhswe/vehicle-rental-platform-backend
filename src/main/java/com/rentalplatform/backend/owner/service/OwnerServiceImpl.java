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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnerServiceImpl implements OwnerService {

    private final UserRepository userRepository;

    private final OwnerRepository ownerRepository;

    private final OwnerMapper ownerMapper;

    private final AuthenticationFacade authenticationFacade;

    @Override
    public OwnerResponse registerOwner(
            RegisterOwnerRequest request) {

        UUID userId =
                authenticationFacade.getCurrentUserId();

        User user =
                userRepository.findById(userId)
                              .orElseThrow(
                                      () -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (ownerRepository.existsByUserId(userId)) {
            throw new AppException(
                    ErrorCode.OWNER_ALREADY_EXISTS
            );
        }

        VehicleOwner owner =
                VehicleOwner.builder()
                            .user(user)
                            .businessName(
                                    request.getBusinessName())
                            .description(
                                    request.getDescription())
                            .verifiedStatus("PENDING")
                            .ratingAvg(BigDecimal.ZERO)
                            .totalVehicles(0)
                            .build();

        ownerRepository.save(owner);

        return ownerMapper.toResponse(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleOwner getCurrentOwner() {

        UUID userId =
                authenticationFacade.getCurrentUserId();

        return ownerRepository.findByUserId(userId)
                              .orElseThrow(
                                      () -> new AppException(
                                              ErrorCode.OWNER_NOT_FOUND
                                      ));
    }
}
