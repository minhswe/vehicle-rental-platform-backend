# Tested Service: CustomUserPrincipal

## Overview

Unit tests for `CustomUserPrincipal` are implemented using:

* JUnit 5

### Purpose

Verify user principal behavior used by Spring Security, including user information exposure, role handling, authority generation, and account status evaluation.

### Dependencies Mocked

* N/A

---

## Covered Scenarios

### 1. Create Custom User Principal

### Method

```java
CustomUserPrincipal(User user)
```

### Success: Create principal successfully

**Test Case**

```java
shouldCreatePrincipalSuccessfully
```

**Input / Context**

* Valid user provided.
* User role is `ADMIN`.
* User status is `ACTIVE`.

**Verified**

* Principal created successfully.
* Email exposed correctly.
* User role exposed correctly.
* Account marked as enabled.

---

### 2. Check Admin Role

### Method

```java
boolean isAdmin()
```

### Success: User is admin

**Test Case**

```java
shouldReturnTrueWhenUserIsAdmin
```

**Input / Context**

* User role is `ADMIN`.
* User status is `ACTIVE`.

**Verified**

* Admin role detected correctly.
* Method returns `true`.

---

### 3. Check Account Status

### Methods

```java
boolean isEnabled()

boolean isAccountNonLocked()
```

### Success: Suspended user is disabled

**Test Case**

```java
shouldReturnFalseWhenUserSuspended
```

**Input / Context**

* User role is `CUSTOMER`.
* User status is `SUSPEND`.

**Verified**

* Account marked as disabled.
* Account marked as locked.
* Security restrictions applied correctly.

---

### 4. Generate User Authorities

### Method

```java
Collection<? extends GrantedAuthority> getAuthorities()
```

### Success: Generate admin authority

**Test Case**

```java
shouldReturnAdminAuthority
```

**Input / Context**

* User role is `ADMIN`.

**Verified**

* Authority collection generated successfully.
* Authority value equals:

```text
ROLE_ADMIN
```

* Spring Security role convention applied correctly.

---

### 5. Validate Constructor Input

### Method

```java
CustomUserPrincipal(User user)
```

### Failure: User is null

**Test Case**

```java
shouldThrowExceptionWhenUserIsNull
```

**Input / Context**

* User parameter is `null`.

**Verified**

* Throws `NullPointerException`.
* Principal creation prevented.

---

## Current Coverage: CustomUserPrincipal

### Covered business flows

* Create authenticated principal
* Retrieve user email
* Retrieve user role
* Check admin privileges
* Generate granted authorities
* Evaluate account enabled status
* Evaluate account lock status

### Covered error flows

* Null user provided during principal creation

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* Security authority generation: Covered
* Account status evaluation: Covered
* Spring Security integration logic: Covered
* Repository interactions: N/A
* External service interactions: N/A
