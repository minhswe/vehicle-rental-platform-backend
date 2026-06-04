# Tested Service: AuthService

## Overview

Unit tests for `AuthService` are implemented using:

* JUnit 5
* Mockito

### Purpose

Verify authentication and authorization business logic independently from database infrastructure, JWT implementation, and Spring Security framework components.

### Dependencies Mocked

* UserRepository
* PasswordEncoder
* JwtService
* RefreshTokenService
* AuthenticationManager
* HttpServletRequest

---

## Covered Scenarios

### 1. Register User

### Method

```java
AuthResponse register(RegisterRequest request)
```

### Success: Register user successfully

**Test Case**

```java
shouldRegisterSuccessfully
```

**Input / Context**

* Email does not already exist.
* Valid registration request provided.

**Verified**

* Email normalized before persistence.
* Password encoded successfully.
* User created with default role `CUSTOMER`.
* User created with status `ACTIVE`.
* Repository save operation executed.
* AuthResponse returned correctly.

---

### Failure: Email already exists

**Test Case**

```java
shouldThrowExceptionWhenEmailExists
```

**Input / Context**

* Email already exists in system.

**Verified**

* Throws `AppException`.
* User creation prevented.
* Repository save operation not executed.
* Password encoder not invoked.

---

### 2. Login

### Method

```java
AuthResponse login(LoginRequest request,
                   HttpServletRequest httpServletRequest)
```

### Success: Login successfully

**Test Case**

```java
login_shouldReturnAuthResponse_whenCredentialsAreValid
```

**Input / Context**

* Valid email and password provided.
* Authentication succeeds.
* User exists in database.

**Verified**

* AuthenticationManager authentication executed.
* Access token generated.
* Refresh token generated.
* Refresh token stored successfully.
* User last login information updated.
* AuthResponse returned with user information and tokens.

---

### Failure: User not found after authentication

**Test Case**

```java
login_shouldThrowException_whenUserNotFound
```

**Input / Context**

* Authentication succeeds.
* User ID extracted from principal.
* User no longer exists in database.

**Verified**

* Throws `AppException`.
* User update operation not executed.
* Login process terminated.

---

### Success: Login with missing User-Agent

**Test Case**

```java
login_shouldUseUnknownDevice_whenUserAgentMissing
```

**Input / Context**

* Authentication succeeds.
* HTTP request does not contain User-Agent header.

**Verified**

* Refresh token created successfully.
* Device name automatically assigned as `"Unknown device"`.
* Refresh token persistence executed correctly.

---

### 3. Refresh Token

### Method

```java
AuthResponse refreshToken(String refreshToken)
```

### Success: Refresh token successfully

**Test Case**

```java
refreshToken_shouldReturnNewTokens
```

**Input / Context**

* Refresh token is valid.
* Associated user exists and is active.

**Verified**

* Refresh token validated successfully.
* User loaded successfully.
* New access token generated.
* New refresh token generated.
* Refresh token rotation executed.
* AuthResponse returned correctly.

---

### Failure: User not found during refresh

**Test Case**

```java
refreshToken_shouldThrowException_whenUserNotFound
```

**Input / Context**

* Refresh token is valid.
* Associated user does not exist.

**Verified**

* Throws `AppException`.
* Token refresh process terminated.

---

### Failure: User suspended

**Test Case**

```java
refreshToken_shouldThrowException_whenUserSuspended
```

**Input / Context**

* Refresh token is valid.
* Associated user status is `SUSPEND`.

**Verified**

* Throws `AppException`.
* User authentication blocked.
* Refresh token rotation not executed.

---

## Current Coverage: AuthService

### Covered business flows

* User registration
* User login
* Refresh token generation
* Refresh token rotation
* Device information handling during login
* JWT access token generation
* JWT refresh token generation

### Covered error flows

* Email already exists
* User not found during login
* User not found during refresh token process
* Suspended user attempting token refresh

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* Authentication flows: Covered
* JWT interactions: Covered
* Refresh token interactions: Covered
* Repository interactions: Covered
* External service interactions: Mocked and Covered
