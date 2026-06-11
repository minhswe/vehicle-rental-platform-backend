package com.rentalplatform.backend.owner.service;

import com.rentalplatform.backend.owner.entity.VehicleOwner;

import java.util.UUID;


public interface OwnerContextService {

    VehicleOwner getCurrentOwner();

    UUID getCurrentOwnerId();
}
