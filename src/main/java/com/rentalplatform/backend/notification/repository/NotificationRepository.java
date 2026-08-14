package com.rentalplatform.backend.notification.repository;

import com.rentalplatform.backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends

        JpaRepository<Notification, UUID> {
}
