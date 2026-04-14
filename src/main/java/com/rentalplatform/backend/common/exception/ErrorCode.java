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
    USER_SUSPENDED(403, "User suspended");
    //AUTH MODULE

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
