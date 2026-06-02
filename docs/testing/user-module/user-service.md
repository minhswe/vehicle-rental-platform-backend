
# Tested Service: UserServiceImpl

## Overview

Unit tests for `UserServiceImpl` are implemented using:

* JUnit 5
* Mockito

The purpose of these tests is to verify business logic independently from database, file storage, and authentication infrastructure.

### Dependencies Mocked

* UserRepository
* PasswordEncoder
* StorageService
* RefreshTokenService
* MultipartFile

### Covered Scenarios

### 1. Get User Profile

#### Success

* Retrieve user profile by user id.
* Return user information correctly.

**Test Case**

* `shouldGetProfileSuccessfully`

#### Failure

* User does not exist.

**Test Case**

* `shouldThrowWhenUserNotFound`

---

### 2. Update User Profile

#### Success

* Update full name.
* Update phone number.
* Persist updated information.

**Test Case**

* `shouldUpdateProfileSuccessfully`

Verified:

* User information updated correctly.
* Repository save operation executed.

---

### 3. Upload Avatar

#### Success

* Upload avatar for a user without an existing avatar.

**Test Case**

* `shouldUploadAvatarSuccessfully`

Verified:

* File uploaded to storage service.
* Avatar URL updated.
* User saved successfully.

#### Success (Replace Existing Avatar)

* Delete old avatar before uploading a new one.

**Test Case**

* `shouldDeleteOldAvatarBeforeUpload`

Verified:

* Previous avatar removed from storage.
* New avatar uploaded.
* User updated successfully.

---

### 4. Change Password

#### Success

* User changes password successfully.

**Test Case**

* `shouldChangePasswordSuccessfully`

Verified:

* Current password validated.
* New password encoded.
* Password updated.
* User saved.
* All refresh tokens revoked.

---

#### Failure - Social Account

Users authenticated via social login are not allowed to change password.

**Test Case**

* `shouldThrowWhenSocialAccount`

Verified:

* PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL_ACCOUNT exception thrown.

---

#### Failure - Incorrect Current Password

Current password does not match stored password.

**Test Case**

* `shouldThrowWhenCurrentPasswordIncorrect`

Verified:

* CURRENT_PASSWORD_IS_INCORRECT exception thrown.

---

#### Failure - Same Password

New password must differ from current password.

**Test Case**

* `shouldThrowWhenNewPasswordSameAsCurrent`

Verified:

* NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD exception thrown.

---

### 5. Logout

#### Success

Revoke refresh token during logout.

**Test Case**

* `shouldLogoutSuccessfully`

Verified:

* Refresh token revoked successfully.

## Current Coverage: UserServiceImpl

Covered business flows:

* Get profile
* Update profile
* Upload avatar
* Replace existing avatar
* Change password
* Logout

Covered error flows:

* User not found
* Social account password change restriction
* Invalid current password
* Duplicate password validation

Coverage status:

* Success paths: Covered
* Validation paths: Covered
* Repository interactions: Covered
* External service interactions: Covered