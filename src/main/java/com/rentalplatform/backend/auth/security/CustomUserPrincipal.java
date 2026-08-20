package com.rentalplatform.backend.auth.security;

import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.constant.UserRole;
import com.rentalplatform.backend.user.constant.UserStatus;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import java.util.UUID;

@Getter

public class CustomUserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UserStatus status;

    public CustomUserPrincipal(User user) {
        if (user == null) {
            throw new NullPointerException("User cannot be null");
        }
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    public CustomUserPrincipal(UUID id, String email, UserRole role, UserStatus status) {
        this.id = id;
        this.email = email;
        this.password = null;
        this.role = role;
        this.status = status;
    }

    public CustomUserPrincipal(UUID id, String email, UserRole role) {
        this(id, email, role, UserStatus.ACTIVE);
    }


    @Override
    public @Nonnull  Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public @Nonnull String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPEND;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
