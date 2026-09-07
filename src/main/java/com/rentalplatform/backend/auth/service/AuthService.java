package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.dto.reponse.AuthResponse;
import com.rentalplatform.backend.auth.dto.request.LoginRequest;
import com.rentalplatform.backend.auth.dto.request.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpServletRequest);

    AuthResponse refreshToken(String oldRefreshToken);

    void logout(String refreshToken);
}
