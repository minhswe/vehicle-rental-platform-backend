package com.rentalplatform.backend.auth.security;

import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.enums.UserRole;
import com.rentalplatform.backend.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserPrincipalTest {

    private User createUser(
            UserRole role,
            UserStatus status
    ) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setEmail("test@gmail.com");
        user.setPassword("123");
        user.setRole(role);
        user.setStatus(status);

        return user;
    }

    @Test
    void shouldCreatePrincipalSuccessfully() {

        // Arrange
        User user = createUser(UserRole.ADMIN, UserStatus.ACTIVE);

        // Act
        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        // Assert
        assertEquals(
                "admin@gmail.com",
                principal.getEmail()
        );

        assertEquals(
                UserRole.ADMIN,
                principal.getRole()
        );

        assertTrue(principal.isEnabled());
    }

    @Test
    @DisplayName("Admin user should be recognized correctly")
    void shouldReturnTrueWhenUserIsAdmin() {

        User user = createUser(UserRole.ADMIN, UserStatus.ACTIVE);

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        assertTrue(principal.isAdmin());
    }

    @Test
    void shouldReturnFalseWhenUserSuspended() {

        User user = createUser(UserRole.CUSTOMER, UserStatus.SUSPEND);

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        assertFalse(principal.isEnabled());

        assertFalse(principal.isAccountNonLocked());
    }

    @Test
    void shouldReturnAdminAuthority() {

        User user = createUser(UserRole.ADMIN, UserStatus.ACTIVE);

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        assertEquals(
                "ROLE_ADMIN",
                principal.getAuthorities()
                         .iterator()
                         .next()
                         .getAuthority()
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {

        assertThrows(
                NullPointerException.class,
                () -> new CustomUserPrincipal(null)
        );
    }
}
