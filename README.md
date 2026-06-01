# Vehicle Rental Platform Backend
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Neon](https://img.shields.io/badge/Neon-Cloud_Database-brightgreen)
![Cloudinary](https://img.shields.io/badge/Cloudinary-Media_Storage-blue)

## Current Progress

| Module | Status |
|----------|----------|
| Authentication | ✅ Completed |
| Vehicle | 🚧 In Progress |
| Booking | ⏳ Planned |
| Payment | ⏳ Planned |
| Notification | ⏳ Planned |
| Review | ⏳ Planned |

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

Coverage Summary:

| Module | Line Coverage |
|----------|----------|
| AuthService | 98% |
| JwtService | 95% |
| RefreshTokenService | 93% |

Generate Coverage Report

```bash
./gradlew clean test jacocoTestReport
````

## Architecture
```
Client → Spring Security → Controller → Service → Repository → PostgreSQL
```
## API documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html

- OpenAPI Specification: docs/openapi.json

- Generated automatically via GitHub Actions.

## Database design

See here in [dbdiagram](https://dbdiagram.io/d/Vehicle-rental-platform-69cb4b4cfb2db18e3b41ae5c)
