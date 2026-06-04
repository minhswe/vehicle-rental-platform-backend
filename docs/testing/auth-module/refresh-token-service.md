# Tested Service: RefreshTokenService

## Overview

Unit tests for `RefreshTokenService` are implemented using:

* JUnit 5
* Mockito

### Purpose

Verify refresh token lifecycle management, including token creation, validation, rotation, revocation, and cleanup independently from database infrastructure and JWT implementation.

### Dependencies Mocked

* RefreshTokenRepository
* PasswordEncoder
* JwtService

---

## Covered Scenarios

### 1. Create Refresh Token

### Method

```java
void createRefreshToken(UUID userId,
                        String rawToken,
                        String device,
                        String ipAddress)
```

### Success: Create refresh token

**Test Case**

```java
shouldCreateRefreshToken
```

**Input / Context**

* Valid user ID provided.
* Valid refresh token provided.
* Device and IP information available.

**Verified**

* JTI extracted from JWT token.
* Refresh token entity created successfully.
* Token hash generated and stored.
* Device information stored.
* IP address stored.
* Token marked as active (not revoked).
* Repository save operation executed.

---

### 2. Verify Refresh Token

### Method

```java
RefreshToken verifyRefreshToken(String rawToken)
```

### Success: Verify valid refresh token

**Test Case**

```java
shouldVerifyRefreshToken
```

**Input / Context**

* Refresh token exists.
* Token is active.
* Token is not expired.
* Token hash matches stored value.

**Verified**

* JTI extracted successfully.
* Refresh token loaded from repository.
* Token validation completed successfully.
* Valid RefreshToken entity returned.

---

### Failure: Token revoked

**Test Case**

```java
shouldThrowWhenTokenRevoked
```

**Input / Context**

* Refresh token exists.
* Token already marked as revoked.

**Verified**

* Throws `AppException`.
* Error code:

    * `TOKEN_REVOKED`

---

### Failure: Token expired

**Test Case**

```java
shouldThrowWhenTokenExpired
```

**Input / Context**

* Refresh token exists.
* Expiration date already passed.

**Verified**

* Throws `AppException`.
* Error code:

    * `TOKEN_EXPIRED`

---

### Failure: Token hash mismatch

**Test Case**

```java
shouldThrowWhenTokenHashMismatch
```

**Input / Context**

* Refresh token exists.
* Incoming token hash does not match stored hash.

**Verified**

* Throws `AppException`.
* Error code:

    * `INVALID_TOKEN`

---

### 3. Rotate Refresh Token

### Method

```java
void rotateRefreshToken(String oldToken,
                        String newToken)
```

### Success: Rotate refresh token

**Test Case**

```java
shouldRotateRefreshToken
```

**Input / Context**

* Existing refresh token is valid.
* New refresh token generated.

**Verified**

* Old token loaded successfully.
* Old token marked as revoked.
* New token entity created.
* New token hash generated.
* Repository save operations executed.
* Token rotation completed successfully.

---

### 4. Revoke Refresh Token

### Method

```java
void revokeToken(String rawToken)
```

### Success: Revoke token

**Test Case**

```java
shouldRevokeToken
```

**Input / Context**

* Refresh token exists.

**Verified**

* Token loaded successfully.
* Token marked as revoked.
* Repository save operation executed.

---

### 5. Revoke All User Tokens

### Method

```java
void revokeAllByUser(UUID userId)
```

### Success: Revoke all active user tokens

**Test Case**

```java
shouldRevokeAllUserTokens
```

**Input / Context**

* User has multiple active refresh tokens.

**Verified**

* Active tokens retrieved successfully.
* All tokens marked as revoked.
* Repository saveAll operation executed.

---

### 6. Delete Expired Tokens

### Method

```java
void deleteExpiredTokens()
```

### Success: Delete expired refresh tokens

**Test Case**

```java
shouldDeleteExpiredTokens
```

**Input / Context**

* Expired refresh tokens exist in storage.

**Verified**

* Repository cleanup method executed.
* Expired tokens removed successfully.

---

## Current Coverage: RefreshTokenService

### Covered business flows

* Create refresh token
* Verify refresh token
* Rotate refresh token
* Revoke single token
* Revoke all user tokens
* Delete expired tokens
* Token hashing and persistence

### Covered error flows

* Revoked token validation
* Expired token validation
* Invalid token hash validation

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* Refresh token lifecycle: Covered
* Token rotation: Covered
* Token revocation: Covered
* Token cleanup: Covered
* Repository interactions: Covered
* JWT interactions: Covered
* External service interactions: N/A
