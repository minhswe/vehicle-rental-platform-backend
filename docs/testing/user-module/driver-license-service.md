# Tested Service: DriverLicenseServiceImpl
## Overview

Unit tests for `UserServiceImpl` are implemented using:

* JUnit 5
* Mockito

Purpose: Verify Driver License business logic independently from database, cloud storage, and external infrastructure

The objective is to ensure:

* Driver license registration rules are enforced correctly.
* Duplicate license prevention works correctly.
* License update and resubmission workflows behave as expected.
* File upload and deletion interactions are executed properly.
* Verification status transitions follow business requirements.


### Dependencies Mocked

* DriverLicenseRepository
* DriverLicenseMapper
* StorageService
* MultipartFile (frontImage)
* MultipartFile (backImage)

### Covered Scenarios

### 1. Upload Driver License

### Method

```java
DriverLicenseResponse upload(
    UUID userId,
    String licenseNumber,
    LocalDate expiryDate,
    MultipartFile frontImage,
    MultipartFile backImage
)
```

### Success

#### Test Case

```java
shouldUploadSuccessfully
```

#### Input / Context

* User does not already own a driver license.
* License number does not exist.
* Front image upload succeeds.
* Back image upload succeeds.

#### Verified

* Front image uploaded to storage.
* Back image uploaded to storage.
* Driver license entity created.
* Verification status initialized as PENDING.
* Repository save operation executed.
* Response returned successfully.

---

### Failure

#### Test Case

```java
shouldThrowWhenUserAlreadyHasLicense
```

#### Input / Context

* User already owns a driver license.

#### Verified

* AppException thrown.
* ErrorCode.DRIVER_LICENSE_ALREADY_EXISTS returned.
* No upload operation executed.
* No save operation executed.

---

### Failure

#### Test Case

```java
shouldThrowWhenLicenseNumberExists
```

#### Input / Context

* Another driver license already uses the same license number.

#### Verified

* AppException thrown.
* ErrorCode.DRIVER_LICENSE_ALREADY_EXISTS returned.
* No upload operation executed.
* No save operation executed.

---

### 2. Get Driver License

### Method

```java
DriverLicenseResponse getMyLicense(UUID userId)
```

### Success

#### Test Case

```java
shouldGetLicenseSuccessfully
```

#### Input / Context

* Driver license exists for user.

#### Verified

* Repository lookup executed.
* Mapper conversion executed.
* Driver license returned successfully.

---

### Failure

#### Test Case

```java
shouldThrowWhenLicenseNotFound
```

#### Input / Context

* User has no driver license.

#### Verified

* AppException thrown.
* ErrorCode.DRIVER_LICENSE_NOT_FOUND returned.

---
### 3. Update Driver License

### Method

```java
DriverLicenseResponse update(
    UUID userId,
    UpdateDriverLicenseRequest request
)
```

### Success

#### Test Case

```java
shouldUpdateSuccessfully
```

#### Input / Context

* Driver license exists.
* New license number is available.

#### Verified

* License number updated.
* Expiry date updated when provided.
* Verification status reset to PENDING.
* Rejected reason cleared.
* Verification metadata cleared.
* Repository save executed.
* Updated response returned.

---

### Failure

#### Test Case

```java
shouldThrowWhenUpdatingDuplicateLicenseNumber
```

#### Input / Context

* Requested license number already belongs to another user.

#### Verified

* AppException thrown.
* ErrorCode.DRIVER_LICENSE_ALREADY_EXISTS returned.
* No update persisted.

---

### 4. Delete Driver License

### Method

```java
void delete(UUID userId)
```

### Success

#### Test Case

```java
shouldDeleteSuccessfully
```

#### Input / Context

* Driver license exists.

#### Verified

* Front image removed from storage.
* Back image removed from storage.
* Driver license removed from repository.

---

### 5. Resubmit Driver License

### Method

```java
DriverLicenseResponse resubmit(UUID userId)
```

### Success

#### Test Case

```java
shouldResubmitSuccessfully
```

#### Input / Context

* Driver license status is REJECTED.

#### Verified

* Status changed to PENDING.
* Rejected reason cleared.
* Repository save executed.
* Updated response returned.

---

### Failure

#### Test Case

```java
shouldThrowWhenLicenseNotRejected
```

#### Input / Context

* License status is not REJECTED.

#### Verified

* AppException thrown.
* ErrorCode.DRIVER_LICENSE_NOT_REJECTED returned.
* No update persisted.

### Current Coverage: DriverLicenseServiceImpl

### Covered Business Flows

* Upload new driver license.
* Upload front and back images.
* Retrieve driver license information.
* Update license information.
* Reset verification workflow after update.
* Delete driver license and associated images.
* Resubmit rejected license for review.

### Covered Error Flows

* User already owns a driver license.
* Duplicate license number detected.
* Driver license not found.
* Updating with an existing license number.
* Resubmitting a non-rejected license.

### Coverage Status

| Category                     | Status  |
| ---------------------------- | ------- |
| Success paths                | Covered |
| Validation paths             | Covered |
| Repository interactions      | Covered |
| Storage service interactions | Covered |
| Exception handling           | Covered |

### Estimated Coverage

* Line Coverage: ~90-95%
* Branch Coverage: High
* Business Rule Coverage: High
