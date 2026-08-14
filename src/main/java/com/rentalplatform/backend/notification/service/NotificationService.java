package com.rentalplatform.backend.notification.service;

import com.rentalplatform.backend.booking.event.BookingCancelledEvent;
import com.rentalplatform.backend.booking.event.BookingConfirmedEvent;
import com.rentalplatform.backend.booking.event.BookingCreatedEvent;
import com.rentalplatform.backend.booking.event.BookingRejectedEvent;
import com.rentalplatform.backend.notification.dto.response.NotificationResponse;
import com.rentalplatform.backend.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    void create(Notification notification);

    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    void markAsRead(UUID notificationId);

    void markAllAsRead();
}
