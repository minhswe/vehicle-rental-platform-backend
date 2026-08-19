# Vehicle Rental Platform Backend

![Java CI](https://github.com/minhswe/vehicle-rental-platform-backend/actions/workflows/java-ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/minhswe/vehicle-rental-platform-backend/branch/master/graph/badge.svg)](...)


## Tech Stack

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Neon](https://img.shields.io/badge/Neon-Cloud_Database-brightgreen)
![Cloudinary](https://img.shields.io/badge/Cloudinary-Media_Storage-blue)

- **Backend Framework:** Spring Boot (Java)
- **Security:** Spring Security & JWT
- **Data & Storage:** PostgreSQL (Neon Cloud), Cloudinary
- **Testing:** JUnit 5 & Mockito *(Unit Testing)*
- **Code Analysis:** SonarQube *(Static Code Analysis & Test Coverage)*
- **Build Tool & Documentation:** Gradle, Swagger

## Current Progress

| Module         | Status      |
|----------------|-------------|
| Authentication | ✅ Completed |
| Vehicle        | ✅ Completed |
| Booking        | ✅ Completed |
| Payment        | ✅ Completed |
| Wallet         | ✅ Completed |
| Notification   | ⏳ Planned   |
| Review         | ⏳ Planned   |

## System features (Detailed in docs/)

### Authentication & Security
- JWT Authentication (Access & Refresh Tokens)
- Refresh Token Rotation
- Spring Security Integration
- Role-Based Access Control (RBAC)
- BCrypt Password Hashing
- Device & IP Tracking
- Stateless Session Management

### Booking module
**Features**
- Booking creation and management
- Booking approval/rejection by vehicle owners
- Booking cancellation
- Booking history retrieval with pagination

**Technical Highlights**
- Overlapping booking detection
- Role-based authorization
- Booking status audit logging
- Transaction management
- Payment refund integration

### DevOps
- GitHub Actions CI/CD
- Automated Build & Test
- JaCoCo Code Coverage
- Codecov Integration
- SonarQube Static Code Analysis
- OpenAPI Specification Generation (Github page)
- API Documentation Deployment (GitHub Page)
- Build Artifact Upload

### Testing

- JUnit 5
- Mockito
- Jacoco

### Testing Strategy

- Unit testing using JUnit 5 and Mockito
- Automated CI test execution through GitHub Actions
- Code coverage monitoring with JaCoCo and Codecov

Generate Coverage Report

```shell
./gradlew clean test jacocoTestReport
````

## Architecture

```
Client → Spring Security → Controller → Service → Repository → PostgreSQL
```

## API documentation

- Swagger UI: https://minhswe.github.io/vehicle-rental-platform-backend/

- OpenAPI Specification: https://minhswe.github.io/vehicle-rental-platform-backend/openapi.json

- Generated automatically via GitHub Actions.

## Database design

See here in [dbdiagram](https://dbdiagram.io/d/Vehicle-rental-platform-69cb4b4cfb2db18e3b41ae5c)

## Getting started (Local Setup)

### Prerequisites

- Java 21
- Gradle

### Installation

1. Clone this repository

```shell
git clone https://github.com/minhswe/vehicle-rental-platform-backend.git
```

2. Copy `.env.example` and update the required values

3. Build the application

```shell
./gradlew clean build
```

4. Run the application

```shell
./gradlew bootRun
```
