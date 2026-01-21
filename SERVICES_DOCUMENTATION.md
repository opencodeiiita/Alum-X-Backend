# AlumX Backend - Services Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication System](#authentication-system)
3. [Services](#services)
   - [Auth Service](#1-auth-service)
   - [User Service](#2-user-service)
   - [User Aura Service](#3-user-aura-service)
   - [User Search Service](#4-user-search-service)
   - [Chat Service](#5-chat-service)
   - [Group Chat Service](#6-group-chat-service)
   - [Group Message Service](#7-group-message-service)
   - [Job Post Service](#8-job-post-service)
   - [Connection Service](#9-connection-service)
   - [Notification Service](#10-notification-service)
   - [Resume Service](#11-resume-service)

---

## Overview

AlumX Backend is a Spring Boot-based alumni networking platform that provides services for user management, messaging, job posting, and professional networking. The application uses JWT-based authentication for secure access control.

**Tech Stack:**
- Spring Boot 4.0.1
- Java 21
- PostgreSQL
- JWT Authentication
- Spring Security

---

## Authentication System

### Implementation
The authentication system uses **JWT (JSON Web Tokens)** for stateless authentication with role-based access control.

### Components

#### 1. **JwtTokenProvider**
- Generates JWT tokens containing user ID, email, username, and role
- Validates and parses JWT tokens
- Token expiration: 1 hour (3600000ms)
- Uses HMAC-SHA with configurable secret key

#### 2. **JwtAuthenticationFilter**
- Intercepts all HTTP requests
- Extracts JWT token from `Authorization: Bearer <token>` header
- Validates token and sets authentication in SecurityContext
- Assigns roles with `ROLE_` prefix (e.g., `ROLE_STUDENT`)

#### 3. **SecurityConfig**
- Configures security filter chain
- Defines public endpoints (no authentication required)
- Enforces authentication for protected routes
- Stateless session management

### Public Endpoints (No Authentication)
```
POST   /api/auth/login
POST   /api/auth/register
POST   /api/users          (user registration)
GET    /health
```

### Protected Endpoints
All other endpoints require valid JWT token in Authorization header.

### User Roles
- **STUDENT** - Students seeking mentorship and opportunities
- **ALUMNI** - Alumni sharing experiences and job opportunities
- **PROFESSOR** - Faculty members acting as moderators

### Token Structure
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "username": "john_doe",
  "role": "ALUMNI",
  "iat": 1704456000,
  "exp": 1704459600
}
```

---

## Services

## 1. Auth Service

**Package:** `com.opencode.alumxbackend.auth.service`

### Description
Handles user authentication, registration, and JWT token generation. Validates credentials and creates secure user accounts with encrypted passwords.

### Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "emailOrUsername": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenExpiryTime": 3600000,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "name": "John Doe",
    "role": "STUDENT"
  }
}
```

**Status Codes:**
- `200 OK` - Login successful
- `401 Unauthorized` - Invalid credentials

---

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenExpiryTime": 3600000,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "name": "John Doe",
    "role": "STUDENT"
  }
}
```

**Validations:**
- Email must be unique and valid format
- Username must be unique
- Password minimum 6 characters
- Role must be: STUDENT, ALUMNI, or PROFESSOR

**Status Codes:**
- `201 Created` - Registration successful
- `400 Bad Request` - Validation error or duplicate email/username

---

## 2. User Service

**Package:** `com.opencode.alumxbackend.users.service`

### Description
Manages user profiles including creation, retrieval, and updates. Handles comprehensive user data including skills, education, experience, and personal information.

### Endpoints

#### Get User Profile
```http
GET /api/users/{userId}/profile
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "name": "John Doe",
  "email": "john@example.com",
  "about": "Software Engineer passionate about AI",
  "currentCompany": "Tech Corp",
  "currentRole": "Senior Developer",
  "location": "San Francisco, CA",
  "linkedinUrl": "https://linkedin.com/in/johndoe",
  "githubUrl": "https://github.com/johndoe",
  "portfolioUrl": "https://johndoe.dev",
  "skills": ["Java", "Spring Boot", "React"],
  "education": ["B.Tech in CS - MIT 2020"],
  "techStack": ["Java", "Python", "JavaScript"],
  "frameworks": ["Spring", "React", "Node.js"],
  "languages": ["English", "Spanish"],
  "communicationSkills": ["Public Speaking", "Technical Writing"],
  "softSkills": ["Leadership", "Team Management"],
  "experience": ["3 years at Tech Corp"],
  "internships": ["Summer Intern at StartupXYZ"],
  "projects": ["E-commerce Platform", "Chat Application"],
  "certifications": ["AWS Certified Developer"],
  "hobbies": ["Photography", "Hiking"],
  "profileCompleted": true
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `404 Not Found` - User not found

---

#### Get All Users
```http
GET /api/users
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "STUDENT",
    "createdAt": "2026-01-01T10:00:00"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com",
    "role": "ALUMNI",
    "createdAt": "2026-01-02T11:30:00"
  }
]
```

**Status Codes:**
- `200 OK` - Users retrieved successfully

---

#### Update User Profile
```http
PATCH /api/users/{userId}/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "about": "Updated bio",
  "currentCompany": "New Company",
  "currentRole": "Lead Engineer",
  "location": "New York, NY",
  "linkedinUrl": "https://linkedin.com/in/johndoe",
  "githubUrl": "https://github.com/johndoe",
  "portfolioUrl": "https://johndoe.dev",
  "skills": ["Java", "Python", "Go"],
  "education": ["B.Tech in CS - MIT 2020", "M.Tech in AI - Stanford 2022"],
  "techStack": ["Spring Boot", "Django", "FastAPI"],
  "frameworks": ["Spring", "React", "Angular"],
  "languages": ["English", "French"],
  "communicationSkills": ["Public Speaking"],
  "softSkills": ["Leadership"],
  "experience": ["5 years in software development"],
  "internships": ["Google Summer Intern 2019"],
  "projects": ["AI Chatbot", "ML Pipeline"],
  "certifications": ["AWS Solutions Architect"],
  "hobbies": ["Reading", "Coding"]
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Status Codes:**
- `200 OK` - Profile updated successfully
- `404 Not Found` - User not found

---

## 3. User Aura Service

**Package:** `com.opencode.alumxbackend.users.service`

### Description
Provides a comprehensive view of user skills, education, and professional attributes. Offers both plain and color-coded responses for UI visualization.

### Endpoints

#### Get User Aura
```http
GET /api/users/{userId}/aura
Authorization: Bearer <token>
```

**Response:**
```json
{
  "skills": ["Java", "Spring Boot"],
  "education": ["B.Tech CS"],
  "techStack": ["Java", "PostgreSQL"],
  "languages": ["English", "Hindi"],
  "frameworks": ["Spring", "React"],
  "communicationSkills": ["Public Speaking"],
  "certifications": ["AWS Certified"],
  "projects": ["E-commerce App"],
  "softSkills": ["Leadership"],
  "hobbies": ["Reading"],
  "experience": ["3 years"],
  "internships": ["Google 2019"]
}
```

**Status Codes:**
- `200 OK` - Aura retrieved successfully
- `404 Not Found` - User not found

---

#### Get Color-Aware Aura
```http
GET /api/users/{userId}/aura/colors
Authorization: Bearer <token>
```

**Response:**
```json
{
  "skills": [
    {"text": "Java", "color": "#FF5733"},
    {"text": "Spring Boot", "color": "#6B8E23"}
  ],
  "education": [
    {"text": "B.Tech CS", "color": "#4169E1"}
  ],
  "techStack": [
    {"text": "Java", "color": "#FF5733"}
  ]
}
```

**Note:** Returns color-coded elements for visual representation in UI.

**Status Codes:**
- `200 OK` - Color-aware aura retrieved successfully
- `404 Not Found` - User not found

---

## 4. User Search Service

**Package:** `com.opencode.alumxbackend.search.service`

### Description
Enables searching for users by name, email, username, or other profile attributes. Supports fuzzy matching for flexible search results.

### Endpoints

#### Search Users
```http
GET /api/users/search?query=john
Authorization: Bearer <token>
```

**Query Parameters:**
- `query` (required) - Search term (minimum 1 character)

**Response:**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "STUDENT",
    "createdAt": "2026-01-01T10:00:00"
  },
  {
    "id": 5,
    "name": "Johnny Smith",
    "email": "johnny@example.com",
    "role": "ALUMNI",
    "createdAt": "2026-01-03T14:20:00"
  }
]
```

**Search Criteria:**
- Username (case-insensitive)
- Name (case-insensitive)
- Email (case-insensitive)

**Status Codes:**
- `200 OK` - Search completed successfully
- `400 Bad Request` - Empty search query

---

## 5. Chat Service

**Package:** `com.opencode.alumxbackend.chat.service`

### Description
Manages one-on-one messaging between users. Creates chat conversations and stores messages with automatic chat room creation.

### Endpoints

#### Send Message
```http
POST /api/chats/send
Authorization: Bearer <token>
Content-Type: application/json

{
  "senderId": 1,
  "recieverId": 2,
  "content": "Hello! How are you?"
}
```

**Response:**
```json
{
  "messageId": 101,
  "chatId": 5,
  "senderUsername": "john_doe",
  "receiverUsername": "jane_smith",
  "content": "Hello! How are you?",
  "createdAt": "2026-01-05T10:30:00"
}
```

**Business Logic:**
- Automatically creates chat room if it doesn't exist
- Normalizes user IDs (smaller ID as user1, larger as user2)
- Validates both sender and receiver exist
- Prevents sending messages to self

**Status Codes:**
- `200 OK` - Message sent successfully
- `400 Bad Request` - Sender and receiver cannot be same
- `404 Not Found` - Sender or receiver not found

---

## 6. Group Chat Service

**Package:** `com.opencode.alumxbackend.groupchat.service`

### Description
Creates and manages group conversations with multiple participants. Handles group creation and participant management.

### Endpoints

#### Create Group
```http
POST /api/group-chats
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Project Team",
  "participants": [
    {"userId": 1, "username": "john_doe"},
    {"userId": 2, "username": "jane_smith"},
    {"userId": 3, "username": "bob_johnson"}
  ]
}
```

**Response:**
```json
{
  "groupId": 10,
  "name": "Project Team",
  "participants": [
    {"id": 1, "userId": 1, "username": "john_doe"},
    {"id": 2, "userId": 2, "username": "jane_smith"},
    {"id": 3, "userId": 3, "username": "bob_johnson"}
  ]
}
```

**Status Codes:**
- `200 OK` - Group created successfully
- `400 Bad Request` - Invalid participant data

---

#### Get Group Details
```http
GET /api/group-chats/{groupId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "groupId": 10,
  "name": "Project Team",
  "participants": [
    {"id": 1, "userId": 1, "username": "john_doe"},
    {"id": 2, "userId": 2, "username": "jane_smith"}
  ]
}
```

**Status Codes:**
- `200 OK` - Group details retrieved
- `404 Not Found` - Group not found

---

#### Get User's Groups
```http
GET /api/group-chats/user/{userId}
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "groupId": 10,
    "name": "Project Team",
    "participants": [...]
  },
  {
    "groupId": 15,
    "name": "Study Group",
    "participants": [...]
  }
]
```

**Status Codes:**
- `200 OK` - Groups retrieved successfully

---

## 7. Group Message Service

**Package:** `com.opencode.alumxbackend.groupchatmessages.service`

### Description
Handles messaging within group chats. Manages sending, retrieving, and deleting messages with member validation.

### Endpoints

#### Send Group Message
```http
POST /api/groups/{groupId}/messages
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "content": "Hello everyone!"
}
```

**Response:**
```json
{
  "id": 501,
  "senderUserId": 1,
  "senderUsername": "john_doe",
  "content": "Hello everyone!",
  "createdAt": "2026-01-05T11:00:00"
}
```

**Validations:**
- User must be a member of the group
- Message content cannot be empty
- Group must exist

**Status Codes:**
- `200 OK` - Message sent successfully
- `403 Forbidden` - User not a member of group
- `404 Not Found` - Group not found

---

#### Get Group Messages
```http
GET /api/groups/{groupId}/messages?userId={userId}
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 501,
    "senderUserId": 1,
    "senderUsername": "john_doe",
    "content": "Hello everyone!",
    "createdAt": "2026-01-05T11:00:00"
  },
  {
    "id": 502,
    "senderUserId": 2,
    "senderUsername": "jane_smith",
    "content": "Hi John!",
    "createdAt": "2026-01-05T11:01:00"
  }
]
```

**Status Codes:**
- `200 OK` - Messages retrieved successfully
- `403 Forbidden` - User not a member of group
- `404 Not Found` - Group not found

---

#### Get Group Messages (Paginated)
```http
GET /api/groups/{groupId}/messages/user?userId={userId}&page=0&size=20
Authorization: Bearer <token>
```

**Query Parameters:**
- `userId` (required) - User requesting messages
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Messages per page

**Response:**
```json
{
  "content": [
    {
      "id": 501,
      "senderUserId": 1,
      "senderUsername": "john_doe",
      "content": "Hello!",
      "createdAt": "2026-01-05T11:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalPages": 3,
  "totalElements": 45,
  "last": false,
  "first": true
}
```

**Status Codes:**
- `200 OK` - Paginated messages retrieved
- `400 Bad Request` - Invalid page parameters
- `403 Forbidden` - User not a member
- `404 Not Found` - Group not found

---

#### Delete Group Message
```http
DELETE /api/groups/{groupId}/messages/{messageId}?userId={userId}
Authorization: Bearer <token>
```

**Validations:**
- User must be message sender
- User must be group member

**Status Codes:**
- `200 OK` - Message deleted successfully
- `403 Forbidden` - Not authorized to delete
- `404 Not Found` - Message or group not found

---

#### Search Group Messages
```http
GET /api/group-chats/{groupId}/messages/search
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "keyword": "project",
  "page": 0,
  "size": 10
}
```

**Response:**
```json
{
  "messages": [
    {
      "id": 505,
      "senderUserId": 2,
      "senderUsername": "jane_smith",
      "content": "The project deadline is next week",
      "createdAt": "2026-01-05T12:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

**Status Codes:**
- `200 OK` - Search completed successfully
- `403 Forbidden` - User not a member

---

## 8. Job Post Service

**Package:** `com.opencode.alumxbackend.jobposts.service`

### Description
Enables alumni to share job opportunities with the community. Supports creating posts, liking, commenting, and deleting posts with ownership validation.

### Endpoints

#### Get User's Job Posts
```http
GET /api/users/{userId}/posts
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "postId": 201,
    "username": "john_alumni",
    "description": "Software Engineer position at Tech Corp. Looking for passionate developers...",
    "imageUrls": ["https://example.com/job-image.jpg"],
    "createdAt": "2026-01-05T09:00:00",
    "likesCount": 15,
    "commentsCount": 3
  }
]
```

**Status Codes:**
- `200 OK` - Posts retrieved successfully
- `404 Not Found` - User not found

---

#### Create Job Post
```http
POST /api/job-posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "john_alumni",
  "description": "Exciting opportunity at Tech Corp! We're hiring Senior Software Engineers with 3+ years experience in Java and Spring Boot. Competitive salary and great benefits. Apply now!",
  "imageUrls": [
    "https://example.com/job-image1.jpg",
    "https://example.com/job-image2.jpg"
  ]
}
```

**Response:**
```json
{
  "message": "Job post created successfully",
  "postId": 201,
  "username": "john_alumni",
  "createdAt": "2026-01-05T09:00:00"
}
```

**Validations:**
- Username must exist in database
- Description: 50-5000 characters
- Image URLs must be valid (if provided)
- Maximum 5 images

**Status Codes:**
- `201 Created` - Post created successfully
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Invalid token

---

#### Like Job Post
```http
POST /api/jobs/{postId}/like?userId={userId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "Post liked successfully"
}
```

**Business Logic:**
- User can like a post only once
- Duplicate likes throw error

**Status Codes:**
- `200 OK` - Post liked successfully
- `400 Bad Request` - Already liked
- `404 Not Found` - Post or user not found

---

#### Add Comment
```http
POST /api/jobs/{postId}/comment
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "content": "Great opportunity! Thanks for sharing."
}
```

**Status Codes:**
- `200 OK` - Comment added successfully
- `404 Not Found` - Post or user not found

---

#### Delete Job Post
```http
DELETE /api/jobs/{jobId}?userId={userId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "Job post deleted successfully"
}
```

**Validations:**
- Only post owner can delete
- Post must exist

**Status Codes:**
- `200 OK` - Post deleted successfully
- `403 Forbidden` - Not post owner
- `404 Not Found` - Post not found

---

## 9. Connection Service

**Package:** `com.opencode.alumxbackend.connection.service`

### Description
Manages connection requests between users for networking. Implements LinkedIn-style connection system with pending/accepted states.

### Endpoints

#### Send Connection Request
```http
POST /api/users/{targetUserId}/connect
Authorization: Bearer <token>
Headers:
  X-USER-ID: 1
```

**Response:**
```json
"Connection request sent"
```

**Business Logic:**
- Cannot connect with yourself
- Validates both users exist
- Prevents duplicate connection requests
- Creates connection with PENDING status

**Status Codes:**
- `200 OK` - Request sent successfully
- `400 Bad Request` - Cannot connect with self or duplicate request
- `404 Not Found` - User not found

---

## 10. Notification Service

**Package:** `com.opencode.alumxbackend.notifications.service`

### Description
Manages user notifications for various events like connection requests, messages, and job post interactions.

### Endpoints

#### Create Notification
```http
POST /api/notifications
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "type": "CONNECTION_REQUEST",
  "message": "John Doe sent you a connection request",
  "referenceId": 15
}
```

**Response:**
```json
{
  "id": 301,
  "message": "John Doe sent you a connection request",
  "type": "CONNECTION_REQUEST",
  "referenceId": 15,
  "createdAt": "2026-01-05T10:00:00"
}
```

**Notification Types:**
- CONNECTION_REQUEST
- MESSAGE
- JOB_POST
- COMMENT
- LIKE

**Status Codes:**
- `201 Created` - Notification created
- `400 Bad Request` - Invalid data
- `404 Not Found` - User not found

---

#### Get User Notifications
```http
GET /api/notifications?userId={userId}
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 301,
    "message": "John Doe sent you a connection request",
    "type": "CONNECTION_REQUEST",
    "referenceId": 15,
    "createdAt": "2026-01-05T10:00:00"
  },
  {
    "id": 302,
    "message": "New message from Jane Smith",
    "type": "MESSAGE",
    "referenceId": 42,
    "createdAt": "2026-01-05T09:30:00"
  }
]
```

**Note:** Notifications are ordered by creation date (newest first).

**Status Codes:**
- `200 OK` - Notifications retrieved
- `404 Not Found` - User not found

---

## 11. Resume Service

**Package:** `com.opencode.alumxbackend.resume.service`

### Description
Handles resume file uploads and retrieval. Supports PDF and DOCX formats with file size validation and storage management.

### Endpoints

#### Upload Resume
```http
POST /api/resumes
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
  userId: 1
  file: [resume.pdf]
```

**Response:**
```json
"Resume uploaded successfully"
```

**Validations:**
- File size: Maximum 5MB
- File types: PDF (.pdf) or Word (.docx)
- Overwrites existing resume for user
- Deletes old file when new one uploaded

**Status Codes:**
- `200 OK` - Resume uploaded successfully
- `400 Bad Request` - Invalid file type or size exceeded

---

#### Get User Resume
```http
GET /api/resumes/{userId}
Authorization: Bearer <token>
```

**Response:**
```http
HTTP/1.1 302 Found
Location: /uploads/resumes/1_resume.pdf
```

**Note:** Returns 302 redirect to resume file URL.

**Status Codes:**
- `302 Found` - Redirect to resume file
- `404 Not Found` - Resume not found for user

---

## Error Handling

### Global Exception Handler
All services use a centralized exception handler that returns consistent error responses.

### Error Response Format
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists: user@example.com",
  "timestamp": "2026-01-05T10:30:00"
}
```

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Validation error or bad input
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### Custom Exceptions
- `ResourceNotFoundException` - Entity not found (404)
- `BadRequestException` - Invalid input data (400)
- `UnauthorizedAccessException` - Authentication required (401)
- `ForbiddenException` - Access denied (403)
- `InvalidCredentialsException` - Login failed (401)
- `InvalidResumeException` - Resume validation failed (400)
- `ResumeNotFoundException` - Resume not found (404)
- `GroupNotFoundException` - Group not found (404)
- `UserNotMemberException` - User not in group (403)
- `InvalidMessageException` - Message validation failed (400)

---

## Best Practices

### Authentication
- Always include JWT token in Authorization header
- Token format: `Bearer <token>`
- Tokens expire after 1 hour
- Refresh tokens by re-authenticating

### Validation
- All required fields must be provided
- Minimum length requirements enforced
- Email and URL format validation
- File type and size restrictions

### Security
- Passwords are BCrypt hashed (never stored plain)
- JWT tokens are signed and verified
- CSRF protection disabled (stateless API)
- Role-based access control enforced

### Performance
- Pagination available for large datasets
- Lazy loading for related entities
- Indexes on frequently queried fields
- Efficient database queries

---

## Configuration

### Required Environment Variables
```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/alumx
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_secret_key_min_32_chars
JWT_EXPIRATION=3600000

# File Upload
RESUME_UPLOAD_DIR=uploads/resumes
```

### Application Properties
```properties
server.port=8080
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

---

## Testing

### Test Environment
- Uses H2 in-memory database
- Separate `application-test.properties`
- JUnit 5 + Spring Boot Test
- WebClient for integration tests

### Running Tests
```bash
mvn test
```

---

## Version History

**Current Version:** 0.0.1-SNAPSHOT

---

## Contact & Support

For issues, feature requests, or contributions, please refer to the main repository.

**GitHub:** [opencodeiiita/alum-x-backend](https://github.com/opencodeiiita/alum-x-backend)

---

**Last Updated:** January 5, 2026
