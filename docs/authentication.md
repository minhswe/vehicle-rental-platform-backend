# Authentication Module

## Features
- User Registration
- User Login
- JWT Access Token
- Refresh Token Rotation
- User Suspension Handling
- Device Tracking
- IP Tracking

## Authentication Architecture
```
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
```