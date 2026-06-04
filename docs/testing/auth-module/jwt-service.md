# Tested Service: JwtService

## Overview

Unit tests for `JwtService` are implemented using:

* JUnit 5
* Spring ReflectionTestUtils

### Purpose

Verify JWT generation, token validation, claim extraction, and token expiration handling independently from Spring Security authentication flow.

### Dependencies Mocked

* N/A

### Covered Scenarios

### 1. Generate Access Token

### Method

```java
String generateAccessToken(CustomUserPrincipal principal)
```

### Success: Generate access token

**Test Case**

```java
shouldGenerateAccessToken
```

**Input / Context**

* Valid authenticated user principal provided.

**Verified**

* Access token generated successfully.
* Generated token is not null.
* Generated token is not empty.

---

### 2. Generate Refresh Token

### Method

```java
String generateRefreshToken(CustomUserPrincipal principal)
```

### Success: Generate refresh token

**Test Case**

```java
shouldGenerateRefreshToken
```

**Input / Context**

* Valid authenticated user principal provided.

**Verified**

* Refresh token generated successfully.
* Generated token is not null.
* Generated token is not empty.

---

### 3. Extract Email From Token

### Method

```java
String extractEmail(String token)
```

### Success: Extract email successfully

**Test Case**

```java
shouldExtractEmail
```

**Input / Context**

* Valid JWT access token generated from authenticated user.

**Verified**

* Email extracted successfully.
* Extracted email matches token owner email.

---

### 4. Extract User ID From Token

### Method

```java
UUID extractUserId(String token)
```

### Success: Extract user ID successfully

**Test Case**

```java
shouldExtractUserId
```

**Input / Context**

* Valid JWT access token generated from authenticated user.

**Verified**

* User ID extracted successfully.
* Extracted user ID matches token owner ID.

---

### 5. Extract JWT ID (JTI)

### Method

```java
UUID extractJti(String token)
```

### Success: Extract JTI successfully

**Test Case**

```java
shouldExtractJti
```

**Input / Context**

* Valid JWT access token generated from authenticated user.

**Verified**

* JTI extracted successfully.
* Extracted JTI is not null.

---

### 6. Validate Token

### Method

```java
boolean isTokenValid(String token, User user)
```

### Success: Token is valid

**Test Case**

```java
shouldReturnTrueWhenTokenValid
```

**Input / Context**

* Valid JWT token.
* User matches token owner.

**Verified**

* Token validation returns `true`.

---

### Failure: Email mismatch

**Test Case**

```java
shouldReturnFalseWhenEmailMismatch
```

**Input / Context**

* Valid JWT token.
* User email differs from token email.

**Verified**

* Token validation returns `false`.

---

### 7. Extract Claims From Malformed Token

### Method

```java
String extractEmail(String token)
```

### Failure: Invalid token format

**Test Case**

```java
shouldThrowExceptionWhenTokenMalformed
```

**Input / Context**

* Malformed or invalid JWT token provided.

**Verified**

* Throws `AppException`.
* Invalid token detected correctly.

---

### 8. Check Token Expiration

### Method

```java
boolean isTokenExpired(String token)
```

### Success: Detect expired token

**Test Case**

```java
shouldReturnTrueWhenTokenExpired
```

**Input / Context**

* Token generated with extremely short expiration time.
* Token expiration time has passed.

**Verified**

* Expired token detected successfully.
* Method returns `true`.

---

## Current Coverage: JwtService

### Covered business flows

* Generate access token
* Generate refresh token
* Extract email from token
* Extract user ID from token
* Extract JTI from token
* Validate token ownership
* Check token expiration

### Covered error flows

* Invalid JWT token format
* Token validation with mismatched user information
* Expired token detection

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* JWT generation: Covered
* JWT claim extraction: Covered
* JWT validation: Covered
* Expiration handling: Covered
* Repository interactions: N/A
* External service interactions: N/A
