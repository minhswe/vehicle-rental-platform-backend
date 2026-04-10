package com.rentalplatform.backend.user.entity;

import com.rentalplatform.backend.user.enums.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.rentalplatform.backend.user.enums.UserRole;
import com.rentalplatform.backend.user.enums.UserStatus;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String password;

    private String fullName;

    private String phone;

    private String avatarUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Boolean emailVerified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    private LocalDateTime deletedAt;

    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Provider provider =  Provider.LOCAL;

    private String providerId;

}
