package com.rentalplatform.backend.vehicle.enums;

public enum VehicleStatus {
    AVAILABLE,
    INACTIVE,
    MAINTENANCE,
    DELETED, //soft delete
    RENTED,
    PENDING_VERIFICATION
}
