package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.dto.reponse.AuthResponse;
import com.rentalplatform.backend.auth.dto.request.LoginRequest;
import com.rentalplatform.backend.auth.dto.request.RegisterRequest;
import com.rentalplatform.backend.auth.entity.RefreshToken;
import com.rentalplatform.backend.auth.security.CustomUserPrincipal;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.enums.UserRole;
import com.rentalplatform.backend.user.enums.UserStatus;
import com.rentalplatform.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail()
                                      .trim()
                                      .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(registerRequest.getPassword()))
                        .fullName(registerRequest.getFullName())
                        .phone(registerRequest.getPhone())
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(false)
                        .createdAt(Instant.now())
                        .build();

        userRepository.save(user);


        return AuthResponse.builder()
                           .email(user.getEmail())
                           .role(user.getRole())
                           .fullName(user.getFullName())
                           .userId(user.getId())
                           .build();
    }

    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpServletRequest) {
        String email = loginRequest.getEmail()
                                   .trim()
                                   .toLowerCase();

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                loginRequest.getPassword()
                        )
                );

        CustomUserPrincipal principal =
                (CustomUserPrincipal) Objects.requireNonNull(
                        authentication.getPrincipal()
                );

        User user = userRepository.findById(principal.getId())
                                  .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        //save last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        //Get device and ip address
        String device = httpServletRequest.getHeader("User-Agent");
        if (device == null || device.isBlank()) {
            device = "Unknown device";
        }
        String ipAddress = getClientIp(httpServletRequest);
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = "Unknown ip address";
        }

        refreshTokenService.createRefreshToken(
                user.getId(),
                refreshToken,
                device,
                ipAddress
        );

        return AuthResponse.builder()
                           .accessToken(accessToken)
                           .refreshToken(refreshToken)
                           .email(principal.getEmail())
                           .role(principal.getRole())
                           .userId(principal.getId())
                           .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            //If multiple IP, just take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public AuthResponse refreshToken(String oldRefreshToken) {
        //Verify token
        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(oldRefreshToken);

        //Load user
        User user = userRepository.findById(storedToken.getUserId())
                                  .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        if (!principal.isEnabled()) {
            throw new AppException(ErrorCode.USER_SUSPENDED);
        }


        String newAccessToken =
                jwtService.generateAccessToken(principal);

        String newRefreshToken =
                jwtService.generateRefreshToken(principal);

        //Rotate refresh token
        refreshTokenService.rotateRefreshToken(oldRefreshToken, newRefreshToken);

        return AuthResponse.builder()
                           .accessToken(newAccessToken)
                           .refreshToken(newRefreshToken)
                           .email(user.getEmail())
                           .role(user.getRole())
                           .fullName(user.getFullName())
                           .userId(user.getId())
                           .build();
    }


}
