package com.rentalplatform.backend.user.service;

import com.rentalplatform.backend.user.dto.request.ChangePasswordRequest;
import com.rentalplatform.backend.user.dto.request.UpdateProfileRequest;
import com.rentalplatform.backend.user.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

    UserProfileResponse getMyProfile(UUID userId);

    UserProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request);

    UserProfileResponse uploadAvatar(UUID userId, MultipartFile file);

    void changePassword(UUID userId, ChangePasswordRequest request);

    void logout(String refreshToken);
}
