package com.rentalplatform.backend.auth.dto.reponse;

import com.rentalplatform.backend.user.enums.UserRole;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private UserRole role;
    private String fullName;
    private UUID userId;
}
