package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.dto.reponse.AuthResponse;
import com.rentalplatform.backend.auth.dto.request.LoginRequest;
import com.rentalplatform.backend.auth.dto.request.RegisterRequest;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.enums.UserRole;
import com.rentalplatform.backend.user.enums.UserStatus;
import com.rentalplatform.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .email(user.getEmail())
                .role(user.getRole()
                        .name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
