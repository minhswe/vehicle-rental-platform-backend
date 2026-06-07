# Tested Service: VehicleServiceImpl

## Overview

Unit tests for `VehicleServiceImpl` are implemented using:

* JUnit 5
* Mockito

### Purpose

Verify vehicle management business logic independently from database infrastructure and authentication implementation.

### Dependencies Mocked

* VehicleRepository
* VehicleMapper
* OwnerService

---

## Covered Scenarios

### 1. Create Vehicle

### Method

```java
VehicleResponse createVehicle(CreateVehicleRequest request)
```

### Success: Create vehicle successfully

**Test Case**

```java
createVehicle_ShouldReturnVehicleResponse_WhenValidRequest
```

**Input / Context**

* Valid vehicle creation request provided.
* License plate does not already exist.
* Current owner exists.

**Verified**

* License plate normalized correctly.
* Owner retrieved successfully.
* Vehicle entity created and saved.
* Vehicle mapped to response object.
* Vehicle response returned successfully.

---

### Success: Normalize license plate before validation

**Test Case**

```java
createVehicle_ShouldNormalizeLicensePlate
```

**Input / Context**

* License plate provided in lowercase format.

**Verified**

* License plate converted to uppercase before duplicate validation.
* Repository duplicate check uses normalized value.

---

### Success: Set default values during vehicle creation

**Test Case**

```java
createVehicle_ShouldSetDefaultValues
```

**Input / Context**

* Valid vehicle creation request.

**Verified**

* Vehicle owner assigned.
* Status initialized as AVAILABLE.
* Deleted flag initialized as false.
* CreatedAt timestamp assigned.
* UpdatedAt timestamp assigned.

---

### Failure: Invalid manufacture year

**Test Case**

```java
createVehicle_ShouldThrowException_WhenYearInvalid
```

**Input / Context**

* Manufacture year is outside supported range.

**Verified**

* Throws `AppException`.

* Error code:

    * `INVALID_VEHICLE_YEAR`

* Repository save operation not executed.

---

### Failure: License plate already exists

**Test Case**

```java
createVehicle_ShouldThrowException_WhenLicensePlateExists
```

**Input / Context**

* Vehicle with same license plate already exists.

**Verified**

* Throws `AppException`.

* Error code:

    * `LICENSE_PLATE_ALREADY_EXISTS`

* Repository save operation not executed.

---

## 2. Get Vehicle Detail

### Method

```java
VehicleResponse getVehicleDetail(UUID vehicleId)
```

### Success: Retrieve vehicle detail

**Test Case**

```java
getVehicleDetail_ShouldReturnVehicle
```

**Input / Context**

* Vehicle exists.
* Vehicle status is AVAILABLE.
* Vehicle is not deleted.

**Verified**

* Vehicle retrieved successfully.
* Vehicle mapped to response object.
* Response returned correctly.

---

### Failure: Vehicle not found

**Test Case**

```java
getVehicleDetail_ShouldThrowException_WhenNotFound
```

**Input / Context**

* Vehicle does not exist.
* Vehicle is unavailable or deleted.

**Verified**

* Throws `AppException`.

* Error code:

    * `VEHICLE_NOT_FOUND`

---

## 3. Update Vehicle

### Method

```java
VehicleResponse updateVehicle(
    UUID vehicleId,
    UpdateVehicleRequest request
)
```

### Success: Update vehicle successfully

**Test Case**

```java
updateVehicle_ShouldUpdateVehicle
```

**Input / Context**

* Vehicle belongs to current owner.
* Vehicle is editable.
* New license plate is unique.

**Verified**

* Ownership validated.
* Vehicle updated successfully.
* Mapper update method executed.
* Repository save operation executed.
* Response returned successfully.

---

### Failure: Vehicle already uses duplicated license plate

**Test Case**

```java
updateVehicle_ShouldThrowException_WhenLicensePlateAlreadyExists
```

**Input / Context**

* Another vehicle already uses requested license plate.

**Verified**

* Throws `AppException`.

* Error code:

    * `LICENSE_PLATE_ALREADY_EXISTS`

---

### Failure: Vehicle not owned by current owner

**Test Case**

```java
updateVehicle_ShouldThrowException_WhenVehicleNotOwned
```

**Input / Context**

* Vehicle does not belong to current owner.

**Verified**

* Throws `AppException`.

* Error code:

    * `VEHICLE_NOT_FOUND`

---

### Failure: Vehicle is rented

**Test Case**

```java
updateVehicle_ShouldThrowException_WhenVehicleRented
```

**Input / Context**

* Vehicle status is RENTED.

**Verified**

* Throws `AppException`.

* Error code:

    * `VEHICLE_NOT_EDITABLE`

* Update operation prevented.

---

## 4. Soft Delete Vehicle

### Method

```java
void softDeleteVehicle(UUID vehicleId)
```

### Success: Soft delete vehicle

**Test Case**

```java
softDeleteVehicle_ShouldMarkDeleted
```

**Input / Context**

* Vehicle belongs to current owner.
* Vehicle is available.

**Verified**

* Deleted flag set to true.
* DeletedAt timestamp assigned.
* UpdatedAt timestamp updated.
* Repository save operation executed.

---

### Failure: Vehicle not owned by current owner

**Test Case**

```java
softDeleteVehicle_ShouldThrowException_WhenVehicleNotOwned
```

**Input / Context**

* Vehicle does not belong to current owner.

**Verified**

* Throws `AppException`.

* Error code:

    * `VEHICLE_NOT_FOUND`

---

### Failure: Vehicle is rented

**Test Case**

```java
softDeleteVehicle_ShouldThrowException_WhenVehicleRented
```

**Input / Context**

* Vehicle status is RENTED.

**Verified**

* Throws `AppException`.

* Error code:

    * `VEHICLE_NOT_DELETABLE`

* Delete operation prevented.

---

## Current Coverage: VehicleServiceImpl

### Covered business flows

* Create vehicle
* Retrieve vehicle details
* Update vehicle
* Soft delete vehicle
* Vehicle ownership validation
* License plate normalization

### Covered validation flows

* Invalid manufacture year
* Duplicate license plate on create
* Duplicate license plate on update
* Vehicle ownership validation
* Vehicle edit restrictions
* Vehicle delete restrictions

### Covered error flows

* Vehicle not found
* Vehicle not owned by current owner
* Vehicle already exists
* Invalid vehicle year
* Vehicle not editable
* Vehicle not deletable

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* Repository interactions: Covered
* Mapper interactions: Covered
* Owner service interactions: Covered
* External service interactions: N/A
