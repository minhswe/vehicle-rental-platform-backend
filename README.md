# Vehicle Rental Platform Backend
![Java CI](https://github.com/minhswe/vehicle-rental-platform-backend/actions/workflows/java-ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/minhswe/vehicle-rental-platform-backend/branch/master/graph/badge.svg)](...)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Neon](https://img.shields.io/badge/Neon-Cloud_Database-brightgreen)
![Cloudinary](https://img.shields.io/badge/Cloudinary-Media_Storage-blue)

## Current Progress

| Module | Status        |
|----------|---------|
| Authentication | ✅ Completed         |
| Vehicle | 🚧 In Progress     |
| Booking | ⏳ Planned                   |
| Payment | ⏳ Planned                   |
| Notification | ⏳ Planned                   |
| Review | ⏳ Planned                   |

## Tech stack

- Language: Java
- Framework: Spring Boot
- Security: Spring Security & JWT
- Database: PostgreSQL (Neon Cloud)
- Media Storage: Cloudinary
- Build tool: Gradle
- API Docs: Swagger

## Main Modules

- Auth/ Identity
- Vehicle
- Booking
- Payment
- Notification
- Review

## System features (Detailed in docs/)
### Authentication & Security
- JWT Authentication
- Refresh Token Rotation
- Spring Security Integration
- BCrypt Password Encryption
- Custom UserDetailsService
- Custom UserPrincipal
- Role-Based Access Control (RBAC)
- Device & IP Tracking
- Stateless Session Management
### Updating...

### DevOps

- GitHub Actions CI
- Automated Build & Test
- OpenAPI Specification Generation
- Artifact Upload

### Testing
- JUnit 5
- Mockito
- Jacoco


### Testing Strategy
- Unit testing using JUnit 5 and Mockito
- Automated CI test execution through GitHub Actions
- Code coverage monitoring with JaCoCo and Codecov

Coverage Summary:

| Module         | Line Coverage |
|----------------|---------------|
| Authentication | 95%           |
| File upload    | 100%          |
| User           | 96%           |
| Owner          | 100%          |

Generate Coverage Report

```bash
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
