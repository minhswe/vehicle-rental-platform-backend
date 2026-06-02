# Tested Service: AddressServiceImpl
## Overview

Unit tests for `UserServiceImpl` are implemented using:

* JUnit 5
* Mockito



### Purpose

Verify address management business logic independently from database infrastructure and external systems.

### Dependencies Mocked

* AddressRepository
* AddressMapper

### Covered Scenarios

### 1. Create Address

### Method

```java
AddressResponse create(UUID userId, CreateAddressRequest request)
```

### Success: Create first address automatically as default

**Test Case**

```java
shouldCreateFirstAddressAsDefault
```

**Input / Context**

* User has no existing addresses.
* `countByUserId(userId)` returns `0`.

**Verified**

* Address created successfully.
* Address assigned to correct user.
* Address automatically marked as default.
* Repository save operation executed.

---

### Success: Create address and set as default

**Test Case**

```java
shouldCreateAddressAndSetDefault
```

**Input / Context**

* User already has existing addresses.
* Request contains `isDefault = true`.

**Verified**

* Existing default address cleared.
* New address marked as default.
* Repository save operation executed.

---

### 2. Get All Addresses

### Method

```java
List<AddressResponse> getAll(UUID userId)
```

### Success: Retrieve all addresses

**Test Case**

```java
shouldGetAllAddresses
```

**Input / Context**

* User owns one or more addresses.

**Verified**

* Addresses retrieved successfully.
* Mapping from entity to response executed.
* Returned list size matches repository result.

---

### 3. Get Address By Id

### Method

```java
AddressResponse getById(UUID userId, UUID addressId)
```

### Success: Address found

**Test Case**

```java
shouldGetAddressById
```

**Input / Context**

* Address belongs to user.

**Verified**

* Address retrieved successfully.
* Address mapped to response.

---

### Failure: Address not found

**Test Case**

```java
shouldThrowWhenAddressNotFound
```

**Input / Context**

* Address does not exist.
* Address does not belong to user.

**Verified**

* Throws `AppException`.
* Error code:

    * `ADDRESS_OR_USER_NOT_FOUND`

---

### 4. Update Address

### Method

```java
AddressResponse update(UUID userId,
                       UUID addressId,
                       UpdateAddressRequest request)
```

### Success: Update address information

**Test Case**

```java
shouldUpdateAddress
```

**Input / Context**

* Existing address found.
* Request contains updated fields.

**Verified**

* Mapper update method executed.
* Address updated successfully.
* Repository save operation executed.

---

### Success: Update and set default address

**Test Case**

```java
shouldUpdateAndSetDefaultAddress
```

**Input / Context**

* Existing address found.
* Request contains `isDefault = true`.

**Verified**

* Existing default addresses cleared.
* Updated address marked as default.
* Repository save operation executed.

---

### 5. Delete Address

### Method

```java
void delete(UUID userId, UUID addressId)
```

### Success: Delete address

**Test Case**

```java
shouldDeleteAddress
```

**Input / Context**

* Address exists.

**Verified**

* Address deleted successfully.
* Repository delete operation executed.

---

### Success: Delete default address and assign new default

**Test Case**

```java
shouldAssignNewDefaultAfterDeletingDefaultAddress
```

**Input / Context**

* Deleted address is currently default.
* User still has remaining addresses.

**Verified**

* Address deleted.
* Next available address assigned as default.
* Repository save operation executed.

---

### 6. Set Default Address

### Method

```java
AddressResponse setDefault(UUID userId,
                           UUID addressId)
```

### Success: Set address as default

**Test Case**

```java
shouldSetDefaultAddress
```

**Input / Context**

* Address exists.

**Verified**

* Existing default addresses cleared.
* Target address marked as default.
* Repository save operation executed.

---

### 7. Get Default Address

### Method

```java
AddressResponse getDefault(UUID userId)
```

### Success: Retrieve default address

**Test Case**

```java
shouldGetDefaultAddress
```

**Input / Context**

* User has a default address.

**Verified**

* Default address returned successfully.
* Mapping executed correctly.

---

### Failure: No default address exists

**Test Case**

```java
shouldThrowWhenDefaultAddressNotFound
```

**Input / Context**

* User has no default address.

**Verified**

* Throws `AppException`.
* Error code:

    * `ADDRESS_NOT_FOUND`

## Current Coverage: AddressServiceImpl

### Covered business flows

* Create first address
* Create address as default
* Retrieve all addresses
* Retrieve address by id
* Update address
* Update and change default address
* Delete address
* Delete default address and assign new default
* Set default address
* Retrieve default address

### Covered error flows

* Address not found
* Address does not belong to user
* Default address not found

### Coverage Status

* Success paths: Covered
* Validation paths: Covered
* Repository interactions: Covered
* Mapper interactions: Covered
* External service interactions: N/A
