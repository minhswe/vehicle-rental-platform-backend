package com.rentalplatform.backend.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue
    @Column(unique = true)
    private UUID id;

    private UUID userId;

    @Column(nullable = false, unique = true)
    private UUID tokenId; //JTI

    @Column(nullable = false)
    private String hashedToken;

    private Instant expiryDate;

    private String device;

    private String ipAddress;

    private boolean revoked;

    private Instant createdAt;
}
