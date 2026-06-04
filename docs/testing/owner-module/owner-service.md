# Tested Service: OwnerServiceImpl

## Overview

Unit tests for `OwnerServiceImpl` are implemented using:

* JUnit 5
* Mockito

### Purpose

Verify owner registration and owner retrieval business logic independently from database infrastructure and authentication implementation.

### Dependencies Mocked

* UserRepository
* OwnerRepository
* OwnerMapper
* AuthenticationFacade

### Covered Scenarios

### 1. Register Owner

### Method

```java
OwnerResponse registerOwner(RegisterOwnerRequest request)
```

### Success: Register owner successfully

**Test Case**

```java
registerOwner_success
```

**Input / Context**

* Authenticated user exists.
* User has not registered as an owner before.
* Valid owner registration request provided.

**Verified**

* Current user retrieved from authentication context.
* User existence validated.
* Owner profile created successfully.
* Repository save operation executed.
* Entity mapped to response object correctly.

---

### Failure: User not found

**Test Case**

```java
registerOwner_userNotFound
```

**Input / Context**

* Authenticated user ID returned.
* User does not exist in database.

**Verified**

* Throws `AppException`.

* Error code:

    * `USER_NOT_FOUND`

* Repository save operation not executed.

---

### Failure: Owner already exists

**Test Case**

```java
registerOwner_alreadyExists
```

**Input / Context**

* User exists.
* Owner profile already registered for current user.

**Verified**

* Throws `AppException`.
* Duplicate owner registration prevented.
* Repository save operation not executed.

---

### 2. Get Current Owner

### Method

```java
VehicleOwner getCurrentOwner()
```

### Success: Retrieve current owner

**Test Case**

```java
getCurrentOwner_success
```

**Input / Context**

* Authenticated user exists.
* Owner profile exists for current user.

**Verified**

* Current user ID retrieved successfully.
* Owner profile retrieved from repository.
* Returned owner information matches stored data.

---

### Failure: Owner not found

**Test Case**

```java
getCurrentOwner_notFound
```

**Input / Context**

* Authenticated user exists.
* No owner profile found for current user.

**Verified**

* Throws `AppException`.
* Owner retrieval fails correctly.

---

## Current Coverage: OwnerServiceImpl

### Covered business flows

* Register owner account
* Retrieve current owner profile

### Covered error flows

* User not found during owner registration
* Owner already exists
* Owner profile not found

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* Repository interactions: Covered
* Mapper interactions: Covered
* Authentication interactions: Covered
* External service interactions: N/A
