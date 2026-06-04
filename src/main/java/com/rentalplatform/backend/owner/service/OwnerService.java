package com.rentalplatform.backend.owner.service;

import com.rentalplatform.backend.owner.dto.request.RegisterOwnerRequest;
import com.rentalplatform.backend.owner.dto.response.OwnerResponse;
import com.rentalplatform.backend.owner.entity.VehicleOwner;

public interface OwnerService {
    OwnerResponse registerOwner(
            RegisterOwnerRequest request);

    VehicleOwner getCurrentOwner();
}
