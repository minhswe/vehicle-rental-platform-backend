package com.rentalplatform.backend.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //COMMON
    VALIDATION_ERROR(400, "Validation error"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    INTERNAL_ERROR(500, "Internal server error"),
    //COMMON

    //AUTH MODULE
    // USER / RESOURCE
    USER_NOT_FOUND(404, "User not found"),
    EMAIL_ALREADY_EXISTS(409, "Email already exists"),
    PHONE_ALREADY_EXISTS(409, "Phone number already exists"),
    USER_ALREADY_EXISTS(409, "User already exists"),
    INVALID_CREDENTIALS(401, "Invalid credentials"),
    USER_SUSPENDED(403, "User suspended"),
    INVALID_TOKEN(401, "Invalid token"),
    TOKEN_EXPIRED(402, "Token is expired"),
    TOKEN_REVOKED(402, "Token is revoked"),
    PASSWORDS_DO_NOT_MATCH(400, "Passwords do not match"),
    CURRENT_PASSWORD_IS_INCORRECT(400, "Current password is incorrect"),
    NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD(
            400,
            "New password must be different from current password"
    ),
    PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL_ACCOUNT(
            400,
            "Password cannot be changed for social login accounts"
    ),
    //AUTH MODULE

    //UPLOAD MODULE
    FILE_EMPTY(400, "File must not be empty"),
    FILE_TOO_LARGE(400, "File size exceeds maximum allowed size"),
    INVALID_IMAGE_URL(400 , "Invalid image URL"),
    FILE_UPLOAD_FAILED(500, "Failed to upload file"),
    FILE_DELETE_FAILED(500, "Failed to delete file"),

    //ADDRESS
    ADDRESS_OR_USER_NOT_FOUND(400, "Address or User not found"),
    ADDRESS_NOT_FOUND(400, "Address not found");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
