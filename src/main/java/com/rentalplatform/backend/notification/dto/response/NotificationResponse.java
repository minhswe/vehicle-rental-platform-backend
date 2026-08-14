package com.rentalplatform.backend.notification.dto.response;

import com.rentalplatform.backend.notification.constant.NotificationType;
import com.rentalplatform.backend.notification.constant.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;

    private NotificationType type;

    private ReferenceType referenceType;

    private UUID referenceId;

    private String title;

    private String body;

    private Boolean isRead;

    private Instant readAt;

    private Instant createdAt;
}
