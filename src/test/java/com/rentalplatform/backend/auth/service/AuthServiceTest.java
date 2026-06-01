package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.dto.reponse.AuthResponse;
import com.rentalplatform.backend.auth.dto.request.LoginRequest;
import com.rentalplatform.backend.auth.dto.request.RegisterRequest;
import com.rentalplatform.backend.auth.entity.RefreshToken;
import com.rentalplatform.backend.auth.security.CustomUserPrincipal;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.enums.UserRole;
import com.rentalplatform.backend.user.enums.UserStatus;
import com.rentalplatform.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // =========================
    // Mocks
    // =========================
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;


    // =========================
    // Constants
    // =========================
    private static final String RAW_EMAIL = " Admin@Gmail.com ";
    private static final String NORMALIZED_EMAIL = "admin@gmail.com";
    private static final String PASSWORD = "123456";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String FULL_NAME = "Admin";
    private static final String PHONE = "0123456789";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // =========================
    // Helpers
    // =========================
    private RegisterRequest createValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(RAW_EMAIL);
        request.setPassword(PASSWORD);
        request.setFullName(FULL_NAME);
        request.setPhone(PHONE);
        return request;
    }

    // =========================
    // Assertion helper
    // =========================
    private void assertSavedUser(User user) {
        assertEquals(NORMALIZED_EMAIL, user.getEmail());
        assertEquals(ENCODED_PASSWORD, user.getPassword());
        assertEquals(FULL_NAME, user.getFullName());
        assertEquals(PHONE, user.getPhone());
        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getCreatedAt());
    }

    // =========================
    // Tests
    // =========================
    @Test
    @DisplayName("Should register successfully")
    void shouldRegisterSuccessfully() {

        // Arrange
        RegisterRequest request = createValidRequest();

        when(userRepository.existsByEmail(NORMALIZED_EMAIL))
                .thenReturn(false);

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(USER_ID);
                    return user;
                });

        // Act
        AuthResponse response = authService.register(request);

        // Assert - response
        assertNotNull(response);
        assertEquals(NORMALIZED_EMAIL, response.getEmail());
        assertEquals(UserRole.CUSTOMER, response.getRole());
        assertEquals(FULL_NAME, response.getFullName());
        assertNotNull(response.getUserId());

        // Assert - repository interaction (deep check)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertSavedUser(savedUser);

        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {

        //arrange
        RegisterRequest request = createValidRequest();

        when(userRepository.existsByEmail(NORMALIZED_EMAIL))
                .thenReturn(true);

        // Act + Assert
        assertThrows(AppException.class,
                     () -> authService.register(request)
        );

        // Verify no save happened
        verify(userRepository, never()).save(any());

        // Optional strict verification
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Should login successfully")
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {

        // Arrange
        UUID userId = UUID.randomUUID();

        LoginRequest request = new LoginRequest();
        request.setEmail(" Admin@gmail.com ");
        request.setPassword("123456");

        User user = User.builder()
                        .id(userId)
                        .email("admin@gmail.com")
                        .fullName("Admin")
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        Authentication authentication =
                mock(Authentication.class);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(principal))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(principal))
                .thenReturn("refresh-token");

        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn("Chrome");

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn("192.168.1.1");

        // Act
        AuthResponse response =
                authService.login(request, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(userId, response.getUserId());
        assertEquals("admin@gmail.com", response.getEmail());

        verify(refreshTokenService)
                .createRefreshToken(
                        eq(userId),
                        eq("refresh-token"),
                        eq("Chrome"),
                        eq("192.168.1.1")
                );

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw USER_NOT_FOUND when principal user does not exist")
    void login_shouldThrowException_whenUserNotFound() {

        UUID userId = UUID.randomUUID();

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@gmail.com");
        request.setPassword("123456");

        User user = User.builder()
                        .id(userId)
                        .email("admin@gmail.com")
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        Authentication authentication =
                mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> authService.login(request, httpServletRequest)
        );

        verify(userRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Should use unknown device when User-Agent missing")
    void login_shouldUseUnknownDevice_whenUserAgentMissing() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                        .id(userId)
                        .email("admin@gmail.com")
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        Authentication authentication =
                mock(Authentication.class);

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@gmail.com");
        request.setPassword("123456");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(principal))
                .thenReturn("access");

        when(jwtService.generateRefreshToken(principal))
                .thenReturn("refresh");

        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn(null);

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn("1.1.1.1");

        authService.login(request, httpServletRequest);

        verify(refreshTokenService)
                .createRefreshToken(
                        eq(userId),
                        eq("refresh"),
                        eq("Unknown device"),
                        eq("1.1.1.1")
                );
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_shouldReturnNewTokens() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                        .id(userId)
                        .email("admin@gmail.com")
                        .fullName("Admin")
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();

        RefreshToken storedToken = new RefreshToken();
        storedToken.setUserId(userId);

        when(refreshTokenService.verifyRefreshToken("old-token"))
                .thenReturn(storedToken);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(any()))
                .thenReturn("new-access");

        when(jwtService.generateRefreshToken(any()))
                .thenReturn("new-refresh");

        AuthResponse response =
                authService.refreshToken("old-token");

        assertNotNull(response);
        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        assertEquals(userId, response.getUserId());

        verify(refreshTokenService)
                .rotateRefreshToken(
                        "old-token",
                        "new-refresh"
                );
    }

    @Test
    @DisplayName("Should throw USER_NOT_FOUND when refresh token user does not exist")
    void refreshToken_shouldThrowException_whenUserNotFound() {

        UUID userId = UUID.randomUUID();

        RefreshToken storedToken = new RefreshToken();
        storedToken.setUserId(userId);

        when(refreshTokenService.verifyRefreshToken("old-token"))
                .thenReturn(storedToken);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> authService.refreshToken("old-token")
        );
    }

    @Test
    @DisplayName("Should throw USER_SUSPENDED when user is disabled")
    void refreshToken_shouldThrowException_whenUserSuspended() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                        .id(userId)
                        .email("admin@gmail.com")
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.SUSPEND)
                        .build();

        RefreshToken storedToken = new RefreshToken();
        storedToken.setUserId(userId);

        when(refreshTokenService.verifyRefreshToken("old-token"))
                .thenReturn(storedToken);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(
                AppException.class,
                () -> authService.refreshToken("old-token")
        );

        verify(refreshTokenService, never())
                .rotateRefreshToken(any(), any());
    }
}
