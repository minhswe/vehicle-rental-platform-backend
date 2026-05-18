package com.rentalplatform.backend.user.controller;

import com.rentalplatform.backend.user.dto.request.ChangePasswordRequest;
import com.rentalplatform.backend.user.dto.request.UpdateProfileRequest;
import com.rentalplatform.backend.user.dto.response.UserProfileResponse;
import com.rentalplatform.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * GET /users/me
     */
    @GetMapping
    public UserProfileResponse getMyProfile(
            @AuthenticationPrincipal UUID userId
    ) {
        return userService.getMyProfile(userId);
    }

    /**
     * PATCH /users/me
     */
    @PatchMapping
    public UserProfileResponse updateMyProfile(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateMyProfile(userId, request);
    }

    /**
     * POST /users/me/avatar
     */
    @PostMapping("/avatar")
    public UserProfileResponse uploadAvatar(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("file") MultipartFile file
    ) {
        return userService.uploadAvatar(userId, file);
    }

    /**
     * PATCH /users/me/change-password
     */
    @PatchMapping("/change-password")
    public void changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userId, request);
    }
}
