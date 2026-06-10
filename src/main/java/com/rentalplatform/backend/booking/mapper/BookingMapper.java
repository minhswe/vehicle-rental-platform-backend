package com.rentalplatform.backend.booking.mapper;

import com.rentalplatform.backend.booking.dto.response.BookingResponse;
import com.rentalplatform.backend.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "ownerId", source = "owner.id")
    BookingResponse toResponse(Booking booking);
}
