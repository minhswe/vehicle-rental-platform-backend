package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.auth.service.RefreshTokenService;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.user.dto.request.ChangePasswordRequest;
import com.rentalplatform.backend.user.dto.request.UpdateProfileRequest;
import com.rentalplatform.backend.user.dto.response.UserProfileResponse;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.constant.Provider;
import com.rentalplatform.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StorageService storageService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserServiceImpl userService;

    private final UUID userId = UUID.randomUUID();

    private User createUser() {
        User user = new User();

        user.setId(userId);
        user.setEmail("user@gmail.com");
        user.setFullName("John Doe");
        user.setPhone("0123456789");
        user.setPassword("encoded-password");
        user.setProvider(Provider.LOCAL);

        return user;
    }

    // ======================================
    // getMyProfile
    // ======================================

    @Test
    @DisplayName("Should get profile successfully")
    void shouldGetProfileSuccessfully() {

        User user = createUser();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserProfileResponse response =
                userService.getMyProfile(userId);

        assertEquals(userId, response.getId());
        assertEquals("user@gmail.com", response.getEmail());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> userService.getMyProfile(userId)
        );
    }

    // ======================================
    // updateMyProfile
    // ======================================

    @Test
    @DisplayName("Should update profile successfully")
    void shouldUpdateProfileSuccessfully() {

        User user = createUser();

        UpdateProfileRequest request =
                new UpdateProfileRequest();

        request.setFullName("Updated Name");
        request.setPhone("0999999999");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserProfileResponse response =
                userService.updateMyProfile(
                        userId,
                        request
                );

        assertEquals(
                "Updated Name",
                response.getFullName()
        );

        assertEquals(
                "0999999999",
                response.getPhone()
        );

        verify(userRepository)
                .save(user);
    }

    // ======================================
    // uploadAvatar
    // ======================================

    @Test
    @DisplayName("Should upload avatar successfully")
    void shouldUploadAvatarSuccessfully() {

        User user = createUser();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(storageService.upload(
                multipartFile,
                "avatars"
        ))
                .thenReturn("avatar-url");

        UserProfileResponse response =
                userService.uploadAvatar(
                        userId,
                        multipartFile
                );

        assertEquals(
                "avatar-url",
                response.getAvatarUrl()
        );

        verify(storageService)
                .upload(
                        multipartFile,
                        "avatars"
                );
    }

    @Test
    @DisplayName("Should delete old avatar before upload")
    void shouldDeleteOldAvatarBeforeUpload() {

        User user = createUser();

        user.setAvatarUrl("old-avatar");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(storageService.upload(
                multipartFile,
                "avatars"
        ))
                .thenReturn("new-avatar");

        userService.uploadAvatar(
                userId,
                multipartFile
        );

        verify(storageService)
                .delete("old-avatar");

        verify(storageService)
                .upload(
                        multipartFile,
                        "avatars"
                );
    }

    // ======================================
    // changePassword
    // ======================================

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {

        User user = createUser();

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "old-password",
                "encoded-password"
        ))
                .thenReturn(true);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("encoded-new-password");

        userService.changePassword(
                userId,
                request
        );

        assertEquals(
                "encoded-new-password",
                user.getPassword()
        );

        verify(userRepository)
                .save(user);

        verify(refreshTokenService)
                .revokeAllByUser(userId);
    }

    @Test
    @DisplayName("Should throw when social account")
    void shouldThrowWhenSocialAccount() {

        User user = createUser();

        user.setProvider(Provider.GOOGLE);

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(
                AppException.class,
                () -> userService.changePassword(
                        userId,
                        request
                )
        );
    }

    @Test
    @DisplayName("Should throw when current password incorrect")
    void shouldThrowWhenCurrentPasswordIncorrect() {

        User user = createUser();

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("wrong");
        request.setNewPassword("new-password");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong",
                "encoded-password"
        ))
                .thenReturn(false);

        assertThrows(
                AppException.class,
                () -> userService.changePassword(
                        userId,
                        request
                )
        );
    }

    @Test
    @DisplayName("Should throw when new password same as current")
    void shouldThrowWhenNewPasswordSameAsCurrent() {

        User user = createUser();

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("123456");
        request.setNewPassword("123456");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "123456",
                "encoded-password"
        ))
                .thenReturn(true);

        assertThrows(
                AppException.class,
                () -> userService.changePassword(
                        userId,
                        request
                )
        );
    }

    // ======================================
    // logout
    // ======================================

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {

        String refreshToken = "refresh-token";

        userService.logout(refreshToken);

        verify(refreshTokenService)
                .revokeToken(refreshToken);
    }
}
