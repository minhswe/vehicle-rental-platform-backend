package com.rentalplatform.backend.owner.service;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.owner.repository.OwnerRepository;
import com.rentalplatform.backend.vehicle.entity.Vehicle;
import com.rentalplatform.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerContextServiceImpl implements OwnerContextService {

    private final OwnerRepository ownerRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public VehicleOwner getCurrentOwner() {

        UUID userId = authenticationFacade.getCurrentUserId();

        return ownerRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                                     new AppException(ErrorCode.OWNER_NOT_FOUND));
    }
}