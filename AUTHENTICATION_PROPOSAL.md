# Authentication Implementation Proposal for AlumX Backend

## 📋 Executive Summary

This document proposes a comprehensive authentication strategy for the AlumX Backend project. The current system has **no authentication** - all endpoints are publicly accessible with only a dummy token header used for development purposes.

**Recommended Approach**: JWT (JSON Web Token) based authentication with optional OAuth2 integration for social login.

---

## 🔍 1. Current Code Structure Analysis

### 1.1 Project Architecture
```
AlumX Backend (Spring Boot 4.0.1 + Java 21)
├── users/          → User management (models, services, controllers)
├── chat/           → 1-to-1 messaging
├── groupchat/      → Group chat management
├── groupchatmessages/ → Group messaging
├── jobposts/       → Job posting feature
├── resume/         → Resume upload/management
└── common/         → Security config, exception handling
```

### 1.2 Existing Security Configuration
**File**: `src/main/java/com/opencode/alumxbackend/common/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ✅ Already configured
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf->csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // ❌ No protection
        return http.build();
    }
}
```

**Current Issues**:
- All endpoints permit all requests (no authentication)
- CSRF disabled (acceptable for stateless REST API)
- BCrypt password encoder exists but unused for auth
- Dummy token header (`X-DUMMY-TOKEN`) used in some controllers

### 1.3 User Model (Already Auth-Ready)
**File**: `src/main/java/com/opencode/alumxbackend/users/model/User.java`

| Field | Type | Purpose |
|-------|------|---------|
| `id` | Long | Primary key |
| `username` | String | Unique identifier |
| `email` | String | Unique, for login |
| `passwordHash` | String | BCrypt hashed password |
| `role` | UserRole | STUDENT / ALUMNI / PROFESSOR |

**Existing Roles** (`UserRole.java`):
```java
public enum UserRole {
    STUDENT,
    ALUMNI,
    PROFESSOR
}
```

---

## 🎯 2. Where Authentication is Needed

### 2.1 Endpoints Requiring Authentication

| Module | Endpoint | Method | Required Auth Level |
|--------|----------|--------|---------------------|
| **Users** | `/api/users` | POST | Public (Registration) |
| **Users** | `/api/users/{userId}/profile` | GET | Authenticated |
| **JobPosts** | `/api/job-posts` | POST | ALUMNI or PROFESSOR only |
| **JobPosts** | `/api/jobs/{postId}/like` | POST | Authenticated |
| **GroupChat** | `/api/group-chats` | POST | Authenticated |
| **GroupChat** | `/api/group-chats/{groupId}` | GET | Group member only |
| **Resume** | `/api/resumes` | POST | Authenticated (own resume) |
| **Resume** | `/api/resumes/{userId}` | GET | Authenticated |

### 2.2 Access Control Requirements

```
┌─────────────────────────────────────────────────────────────┐
│                    ACCESS CONTROL MATRIX                     │
├─────────────────────────────────────────────────────────────┤
│  PUBLIC (No Auth)                                           │
│  ├── POST /api/users (registration)                         │
│  ├── POST /api/auth/login                                   │
│  └── GET /health                                            │
├─────────────────────────────────────────────────────────────┤
│  AUTHENTICATED (Any logged-in user)                         │
│  ├── GET /api/users/{userId}/profile                        │
│  ├── GET /api/group-chats/user/{userId}                     │
│  ├── POST /api/jobs/{postId}/like                           │
│  └── GET /api/resumes/{userId}                              │
├─────────────────────────────────────────────────────────────┤
│  ROLE-BASED (ALUMNI/PROFESSOR only)                         │
│  └── POST /api/job-posts                                    │
├─────────────────────────────────────────────────────────────┤
│  OWNER-ONLY (User can only access their own resources)      │
│  ├── PUT /api/users/{userId}/profile                        │
│  ├── POST /api/resumes (own resume)                         │
│  └── DELETE /api/job-posts/{postId} (own posts)             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 3. Proposed Authentication Method: JWT

### 3.1 Why JWT?

| Criteria | JWT | Session-Based | OAuth2 Only |
|----------|-----|---------------|-------------|
| Stateless | ✅ Yes | ❌ No | ✅ Yes |
| Scalable | ✅ Excellent | ⚠️ Requires session store | ✅ Good |
| Mobile-friendly | ✅ Yes | ⚠️ Cookie issues | ✅ Yes |
| Implementation complexity | ⚠️ Medium | ✅ Simple | ❌ Complex |
| Fits existing structure | ✅ Yes | ✅ Yes | ⚠️ Partial |

**Recommendation**: JWT with optional OAuth2 for social login (Google/LinkedIn)

### 3.2 Authentication Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                     JWT AUTHENTICATION FLOW                       │
└──────────────────────────────────────────────────────────────────┘

  ┌─────────┐                                      ┌─────────────┐
  │  User   │                                      │   Server    │
  └────┬────┘                                      └──────┬──────┘
       │                                                  │
       │  1. POST /api/auth/login                         │
       │     {email, password}                            │
       │─────────────────────────────────────────────────>│
       │                                                  │
       │                              2. Validate credentials
       │                              3. Generate JWT tokens
       │                                                  │
       │  4. Response: {accessToken, refreshToken}        │
       │<─────────────────────────────────────────────────│
       │                                                  │
       │  5. GET /api/users/profile                       │
       │     Header: Authorization: Bearer <accessToken>  │
       │─────────────────────────────────────────────────>│
       │                                                  │
       │                              6. Validate JWT
       │                              7. Extract user info
       │                              8. Process request
       │                                                  │
       │  9. Response: {user profile data}                │
       │<─────────────────────────────────────────────────│
       │                                                  │
```

### 3.3 Token Structure

```json
// Access Token Payload
{
  "sub": "user@example.com",
  "userId": 123,
  "username": "johndoe",
  "role": "ALUMNI",
  "iat": 1703836800,
  "exp": 1703840400  // 1 hour expiry
}

// Refresh Token Payload
{
  "sub": "user@example.com",
  "userId": 123,
  "type": "refresh",
  "iat": 1703836800,
  "exp": 1704441600  // 7 days expiry
}
```

---

## 📁 4. Affected Files & New Components

### 4.1 Files to MODIFY

| File | Changes Required |
|------|------------------|
| `SecurityConfig.java` | Add JWT filter, configure protected endpoints |
| `UserController.java` | Remove dummy token, add `@PreAuthorize` |
| `UserService.java` | Add `authenticate()` method |
| `UserServiceImpl.java` | Implement authentication logic |
| `JobPostController.java` | Add role-based authorization |
| `GroupChatController.java` | Add ownership verification |
| `ResumeController.java` | Add user ownership checks |
| `pom.xml` | Add JWT dependency (jjwt) |
| `application.properties` | Add JWT secret configuration |

### 4.2 Files to CREATE

```
src/main/java/com/opencode/alumxbackend/
├── auth/
│   ├── controller/
│   │   └── AuthController.java          # Login, register, refresh endpoints
│   ├── dto/
│   │   ├── LoginRequest.java            # Email + password
│   │   ├── LoginResponse.java           # Access + refresh tokens
│   │   ├── RegisterRequest.java         # Registration data
│   │   └── TokenRefreshRequest.java     # Refresh token request
│   ├── service/
│   │   ├── AuthService.java             # Auth interface
│   │   └── AuthServiceImpl.java         # Auth implementation
│   ├── security/
│   │   ├── JwtTokenProvider.java        # Token generation/validation
│   │   ├── JwtAuthenticationFilter.java # Request filter
│   │   └── CustomUserDetailsService.java # Load user for Spring Security
│   └── exception/
│       ├── InvalidCredentialsException.java
│       └── TokenExpiredException.java
```

### 4.3 Dependency Addition (pom.xml)

```xml
<!-- JWT Support -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🔄 5. System Operation After Authentication

### 5.1 New API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | User registration | Public |
| POST | `/api/auth/login` | User login | Public |
| POST | `/api/auth/refresh` | Refresh access token | Refresh token |
| POST | `/api/auth/logout` | Invalidate tokens | Authenticated |

### 5.2 Updated SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/health").permitAll()
                // Role-based endpoints
                .requestMatchers(HttpMethod.POST, "/api/job-posts")
                    .hasAnyRole("ALUMNI", "PROFESSOR")
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 5.3 Controller Authorization Examples

```java
// JobPostController.java - Role-based access
@PostMapping("/api/job-posts")
@PreAuthorize("hasAnyRole('ALUMNI', 'PROFESSOR')")
public ResponseEntity<?> createJobPost(@RequestBody JobPostRequest request) {
    // Only ALUMNI and PROFESSOR can create job posts
}

// UserController.java - Owner-only access
@PutMapping("/api/users/{userId}/profile")
@PreAuthorize("#userId == authentication.principal.id")
public ResponseEntity<?> updateProfile(@PathVariable Long userId, ...) {
    // Users can only update their own profile
}

// GroupChatController.java - Member verification
@GetMapping("/api/group-chats/{groupId}")
public ResponseEntity<?> getGroup(@PathVariable Long groupId, Authentication auth) {
    // Verify user is a member of the group
    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
    if (!groupService.isMember(groupId, userId)) {
        throw new ForbiddenException("Not a member of this group");
    }
}
```

### 5.4 Request/Response Examples

**Login Request**:
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "securePassword123"
}
```

**Login Response**:
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
        "id": 1,
        "username": "johndoe",
        "email": "john@example.com",
        "role": "ALUMNI"
    }
}
```

**Authenticated Request**:
```http
GET /api/users/1/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📊 6. Implementation Phases

### Phase 1: Core JWT Authentication (Week 1)
- [ ] Add JWT dependencies to pom.xml
- [ ] Create JwtTokenProvider utility class
- [ ] Create JwtAuthenticationFilter
- [ ] Create CustomUserDetailsService
- [ ] Update SecurityConfig with JWT filter
- [ ] Create AuthController with login/register endpoints

### Phase 2: Role-Based Access Control (Week 2)
- [ ] Add @PreAuthorize annotations to controllers
- [ ] Implement role-based endpoint protection
- [ ] Add ownership verification for user resources
- [ ] Update GlobalExceptionHandler for auth exceptions

### Phase 3: Token Management (Week 3)
- [ ] Implement refresh token mechanism
- [ ] Add token blacklist for logout
- [ ] Configure token expiration settings
- [ ] Add rate limiting on login attempts

### Phase 4: OAuth2 Integration (Optional - Week 4)
- [ ] Add Spring Security OAuth2 dependencies
- [ ] Configure Google OAuth2 provider
- [ ] Configure LinkedIn OAuth2 provider
- [ ] Create OAuth2 success/failure handlers

---

## ⚙️ 7. Configuration Requirements

### application.properties additions:
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=3600000      # 1 hour in milliseconds
jwt.refresh-token-expiration=604800000   # 7 days in milliseconds

# OAuth2 Configuration (Optional)
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.linkedin.client-id=${LINKEDIN_CLIENT_ID}
spring.security.oauth2.client.registration.linkedin.client-secret=${LINKEDIN_CLIENT_SECRET}
```

### .env additions:
```env
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-characters
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
LINKEDIN_CLIENT_ID=your-linkedin-client-id
LINKEDIN_CLIENT_SECRET=your-linkedin-client-secret
```

---

## 🛡️ 8. Security Considerations

1. **Token Storage**: Access tokens should be stored in memory (not localStorage) on frontend
2. **HTTPS Only**: All authentication endpoints must use HTTPS in production
3. **Password Policy**: Enforce minimum 8 characters, mixed case, numbers
4. **Rate Limiting**: Limit login attempts to 5 per minute per IP
5. **Token Rotation**: Rotate refresh tokens on each use
6. **Audit Logging**: Log all authentication events

---

## 📝 Summary

This proposal outlines a JWT-based authentication system that:
- Fits seamlessly with the existing Spring Boot architecture
- Leverages the already-configured BCrypt password encoder
- Uses the existing User model and UserRole enum
- Provides role-based access control (STUDENT, ALUMNI, PROFESSOR)
- Supports future OAuth2 integration for social login
- Maintains backward compatibility with existing endpoints

The implementation requires creating ~10 new files and modifying ~8 existing files, with an estimated development time of 2-3 weeks for core functionality.
