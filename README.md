# Vechicle Rental Platform Backend
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)

### Current Progress

| Module | Status |
|----------|----------|
| Authentication | ✅ Completed |
| Vehicle | 🚧 In Progress |
| Booking | ⏳ Planned |
| Payment | ⏳ Planned |
| Notification | ⏳ Planned |
| Review | ⏳ Planned |

### Tech stack

- Language: Java
- Framework: Spring Boot
- Security: Srping Security & JWT
- Database: PostgreSQL
- Build tool: Gradle
- API Docs: Swagger

### Main Modules

- Auth/ Identity
- Vehicle
- Booking
- Payment
- Notification
- Review

### System features
- Global exception handling
- JWT Authentication
- Refresh Token Rotation
- Role-Based Access Control (ADMIN/CUSTOMER)
- BCrypt Password Encryption
- Custom UserDetailsService
- Custom UserPrincipal
- Stateless Session Management
- Device & IP Tracking
- Spring Security Integration
- Updating...
- 
### DevOps

- GitHub Actions CI
- Automated Build (already) & Test (not yet)
- OpenAPI Specification Generation
- Artifact Upload


### Architecture

Layered
Controller → Service → Repository → PostgreSQL

### API documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html

- OpenAPI Specification: docs/openapi.json

- Generated automatically via GitHub Actions.

### Database design

See here in [dbdiagram](https://dbdiagram.io/d/Vehicle-rental-platform-69cb4b4cfb2db18e3b41ae5c)

### Security Architecture

```text
Login Request
      ↓
AuthenticationManager
      ↓
DaoAuthenticationProvider
      ↓
CustomUserDetailsService
      ↓
UserRepository
      ↓
CustomUserPrincipal
      ↓
BCryptPasswordEncoder
      ↓
Authenticated User
      ↓
JWT Access Token
      ↓
Refresh Token Rotation

