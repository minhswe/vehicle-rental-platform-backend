package com.rentalplatform.backend.notification.mapper;

import com.rentalplatform.backend.notification.dto.response.NotificationResponse;
import com.rentalplatform.backend.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
