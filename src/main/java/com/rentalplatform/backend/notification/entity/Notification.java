package com.rentalplatform.backend.notification.entity;

import com.rentalplatform.backend.common.entity.AuditEntity;
import com.rentalplatform.backend.notification.constant.NotificationType;
import com.rentalplatform.backend.notification.constant.ReferenceType;
import com.rentalplatform.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends AuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;

    @Column(nullable = false)
    private UUID referenceId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    private Instant readAt;
}
