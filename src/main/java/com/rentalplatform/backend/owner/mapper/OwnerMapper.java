package com.rentalplatform.backend.owner.mapper;

import com.rentalplatform.backend.owner.dto.response.OwnerResponse;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {
    public OwnerResponse toResponse(
            VehicleOwner owner) {

        return OwnerResponse.builder()
                            .id(owner.getId())
                            .userId(owner.getUser().getId())
                            .businessName(owner.getBusinessName())
                            .description(owner.getDescription())
                            .verifiedStatus(owner.getVerifiedStatus())
                            .ratingAvg(owner.getRatingAvg())
                            .totalVehicles(owner.getTotalVehicles())
                            .createdAt(owner.getCreatedAt())
                            .build();
    }
}
