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

    //====================== AUTH MODULE ======================
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

    //====================== UPLOAD MODULE ======================
    FILE_EMPTY(400, "File must not be empty"),
    FILE_TOO_LARGE(413, "File size exceeds maximum allowed size"),
    INVALID_IMAGE_URL(400, "Invalid image URL"),
    FILE_UPLOAD_FAILED(500, "Failed to upload file"),
    FILE_DELETE_FAILED(500, "Failed to delete file"),

    //====================== ADDRESS ======================
    ADDRESS_OR_USER_NOT_FOUND(404, "Address or User not found"),
    ADDRESS_NOT_FOUND(404, "Address not found"),

    //====================== DRIVER LICENSE ======================
    DRIVER_LICENSE_ALREADY_EXISTS(409, "Driver license already exists"),
    DRIVER_LICENSE_NOT_FOUND(404, "Driver license not found"),
    DRIVER_LICENSE_NOT_REJECTED(400, "Driver license is not rejected"),

    // ====================== OWNER ======================
    OWNER_ALREADY_EXISTS(409, "Owner already exists"),
    OWNER_NOT_FOUND(404, "Owner not found"),

    //====================== VEHICLE ======================
    VEHICLE_NOT_FOUND(404, "Vehicle not found"),
    VEHICLE_ACCESS_DENIED(403, "Vehicle access denied"),
    LICENSE_PLATE_ALREADY_EXISTS(409, "License plate already exists"),
    INVALID_VEHICLE_YEAR(400, "Invalid vehicle year"),
    INVALID_LICENSE_PLATE(400, "Invalid license plate"),
    VEHICLE_NOT_EDITABLE(409, "Vehicle not editable"),
    VEHICLE_NOT_DELETABLE(409, "Vehicle not deletable"),

    //====================== VEHICLE IMAGE ======================
    VEHICLE_IMAGE_NOT_FOUND(404, "Vehicle image not found"),
    INVALID_IMAGE_TYPE(400, "Invalid image type"),
    MAX_IMAGES_EXCEEDED(400, "Max images exceeded"),
    MAX_DOCUMENTS_EXCEEDED(400, "Max documents exceeded"),
    VEHICLE_DOCUMENT_NOT_FOUND(404, "Vehicle document not found"),
    INVALID_DOCUMENT_TYPE(400, "Invalid document type"),
    DOCUMENT_ALREADY_EXISTS(409, "Document already exists"),
    DOCUMENT_CANNOT_BE_DELETED(409, "Document cannot be deleted"),
    DOCUMENT_ALREADY_PROCESSED(409, "Document already processed"),

    //====================== BOOKING ======================
    VEHICLE_NOT_AVAILABLE(400, "Vehicle is not available"),
    OWNER_CANNOT_BOOK_OWN_VEHICLE(400, "Owner cannot book their own vehicle"),
    INVALID_TIME_RANGE(400, "Invalid time range"),
    VEHICLE_ALREADY_BOOKED_IN_THIS_TIME_RANGE(400, "Vehicle is already booked in this time range"),
    INVALID_START_TIME(400, "Start time must be in the future"),
    BOOKING_NOT_FOUND(404, "Booking not found"),
    BOOKING_ACCESS_DENIED(403, "You do not have permission to access this booking"),
    INVALID_BOOKING_STATUS(400, "Invalid booking status"),
    BOOKING_ALREADY_CANCELLED(400, "Booking already cancelled"),

    //====================== PAYMENT ======================
    PAYMENT_NOT_FOUND(404, "Payment not found"),
    PAYMENT_ALREADY_EXISTS(409, "Payment already exists"),
    INVALID_PAYMENT_STATE(400, "Invalid payment state"),
    INVALID_PAYMENT_AMOUNT(400, "Invalid payment amount"),
    INVALID_PAYMENT_PROVIDER(400, "Invalid payment provider"),

    //====================== WALLET ======================
    WALLET_NOT_FOUND(404, "Wallet not found"),
    WALLET_LOCKED(403, "Wallet is locked"),
    INSUFFICIENT_WALLET_BALANCE(400, "Insufficient wallet balance"),
    WALLET_HOLD_NOT_FOUND(404, "Wallet hold not found"),
    INVALID_WALLET_HOLD_STATE(400, "Invalid wallet hold state"),
    WALLET_HOLD_ALREADY_EXISTS(409, "Wallet hold already exists"),
    INVALID_WALLET_STATE(400, "Invalid wallet state"),
    INVALID_AMOUNT(400, "Invalid amount"),
    INVALID_PAYMENT(400, "Invalid payment"),
    WALLET_ACCESS_DENIED(403, "You do not have permission to access this wallet");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    }
