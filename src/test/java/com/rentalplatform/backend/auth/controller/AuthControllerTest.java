package com.rentalplatform.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalplatform.backend.auth.dto.reponse.AuthResponse;
import com.rentalplatform.backend.auth.dto.request.LoginRequest;
import com.rentalplatform.backend.auth.dto.request.RefreshTokenRequest;
import com.rentalplatform.backend.auth.dto.request.RegisterRequest;
import com.rentalplatform.backend.auth.service.AuthService;
import com.rentalplatform.backend.common.exception.GlobalExceptionHandler;
import com.rentalplatform.backend.user.constant.UserRole;
import com.rentalplatform.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("Should return 201 Created when register payload is valid")
        void register_Success() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("john.doe@example.com")
                    .password("SecurePass123!")
                    .fullName("John Doe")
                    .phone("+1234567890")
                    .build();

            AuthResponse response = AuthResponse.builder()
                    .userId(java.util.UUID.randomUUID())
                    .email("john.doe@example.com")
                    .fullName("John Doe")
                    .role(UserRole.CUSTOMER)
                    .build();

            given(authService.register(any(RegisterRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.data.fullName").value("John Doe"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when register payload is blank or malformed")
        void register_ValidationFailure_BlankPayload() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("")
                    .password("short")
                    .fullName("")
                    .phone("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.email").exists())
                    .andExpect(jsonPath("$.errors.password").exists())
                    .andExpect(jsonPath("$.errors.fullName").exists())
                    .andExpect(jsonPath("$.errors.phone").exists());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email format is invalid")
        void register_ValidationFailure_InvalidEmail() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("invalid-email")
                    .password("ValidPassword123")
                    .fullName("John Doe")
                    .phone("+1234567890")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.email").value("Email must be valid"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Should return 200 OK when login payload is valid")
        void login_Success() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("john.doe@example.com")
                    .password("SecurePass123!")
                    .build();

            AuthResponse response = AuthResponse.builder()
                    .accessToken("access-token-xyz")
                    .refreshToken("refresh-token-xyz")
                    .email("john.doe@example.com")
                    .role(UserRole.CUSTOMER)
                    .userId(java.util.UUID.randomUUID())
                    .build();

            given(authService.login(any(LoginRequest.class), any())).willReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token-xyz"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-xyz"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when login fields are blank")
        void login_ValidationFailure_BlankFields() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("")
                    .password("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.email").value("Email is required"))
                    .andExpect(jsonPath("$.errors.password").value("Password is required"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh-token")
    class RefreshToken {

        @Test
        @DisplayName("Should return 200 OK when refresh token payload is valid")
        void refreshToken_Success() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();

            AuthResponse response = AuthResponse.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .email("john.doe@example.com")
                    .userId(java.util.UUID.randomUUID())
                    .build();

            given(authService.refreshToken(eq("valid-refresh-token"))).willReturn(response);

            mockMvc.perform(post("/api/v1/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when refresh token is blank")
        void refreshToken_ValidationFailure_BlankToken() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.refreshToken").value("Refresh token is required"));
        }
    }
}
