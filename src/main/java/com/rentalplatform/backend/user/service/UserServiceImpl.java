package com.rentalplatform.backend.user.service;


import com.rentalplatform.backend.auth.service.RefreshTokenService;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.upload.StorageService;
import com.rentalplatform.backend.user.dto.request.ChangePasswordRequest;
import com.rentalplatform.backend.user.dto.request.UpdateProfileRequest;
import com.rentalplatform.backend.user.dto.response.UserProfileResponse;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.enums.Provider;
import com.rentalplatform.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserProfileResponse getMyProfile(UUID userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    @Override
    public UserProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUser(userId);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        return toResponse(user);
    }

    @Override
    public UserProfileResponse uploadAvatar(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //if an avatar existed already
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            storageService.delete(user.getAvatarUrl());
        }

        //upload new avatar into avatars folder
        String avatarUrl = storageService.upload(file, "avatars");

        //update database
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUser(userId);

        if (user.getProvider() != Provider.LOCAL) {
            throw new AppException(ErrorCode.PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL_ACCOUNT);
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_IS_INCORRECT);
        }

        if (request.getCurrentPassword()
                   .equals(request.getNewPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD);
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        // Optional: force login again
        refreshTokenService.revokeAllByUser(user.getId());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);

    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                             .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl()
        );
    }
}
