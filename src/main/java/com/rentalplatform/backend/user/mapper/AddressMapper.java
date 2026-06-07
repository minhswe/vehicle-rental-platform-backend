package com.rentalplatform.backend.user.mapper;

import com.rentalplatform.backend.user.dto.request.CreateAddressRequest;
import com.rentalplatform.backend.user.dto.request.UpdateAddressRequest;
import com.rentalplatform.backend.user.dto.response.AddressResponse;
import com.rentalplatform.backend.user.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Address toEntity(CreateAddressRequest request);

    AddressResponse toResponse(Address address);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void update(
            @MappingTarget Address address,
            UpdateAddressRequest request
    );
}
