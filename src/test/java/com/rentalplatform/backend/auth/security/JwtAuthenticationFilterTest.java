package com.rentalplatform.backend.auth.security;

import com.rentalplatform.backend.auth.service.JwtService;
import com.rentalplatform.backend.user.constant.UserRole;
import com.rentalplatform.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate request with valid JWT without querying database")
    void shouldAuthenticateWithValidJwtWithoutDatabaseAccess() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        UserRole role = UserRole.CUSTOMER;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.extractRole(token)).thenReturn(role);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof CustomUserPrincipal);

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        assertEquals(email, principal.getEmail());
        assertEquals(userId, principal.getId());
        assertEquals(role, principal.getRole());
        assertEquals("ROLE_CUSTOMER", authentication.getAuthorities().iterator().next().getAuthority());

        // Assert database and UserDetailsService were NEVER hit
        verify(userRepository, never()).findByEmail(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        // Assert filter chain execution continued
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when Authorization header is missing")
    void shouldSkipAuthenticationWhenHeaderMissing() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByEmail(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when Authorization header does not start with Bearer ")
    void shouldSkipAuthenticationWhenHeaderNotBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic 12345");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByEmail(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void shouldNotAuthenticateWhenTokenInvalid() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByEmail(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
