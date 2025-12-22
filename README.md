
# AlumX Backend 🚀

**AlumX** is a scalable, backend-heavy alumni networking platform designed to connect **students, alumni, and faculty** through mentorship, knowledge sharing, and AI-powered discovery.  
This repository contains the **Spring Boot backend** powering AlumX.

---

## 📚 Table of Contents

- [Overview](#-overview)
- [Core Features](#-core-features)
- [AI-Powered Capabilities](#-ai-powered-capabilities)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Database Schema (High Level)](#-database-schema-high-level)
- [API Design](#-api-design)
- [Security](#-security)
- [Project Setup](#️-project-setup)
- [Directory Structure](#-directory-structure)
- [Contribution Guidelines](#-contribution-guidelines)
- [Future Enhancements](#-future-enhancements)

---

## 🌐 Overview

AlumX bridges the gap between **college students and alumni** by enabling:

- Verified onboarding using college email, Google, or LinkedIn
- AI-driven alumni discovery using skill & interest matching
- Mentorship workflows and professional networking
- Content creation through blogs and experience sharing

The backend should be designed with **enterprise-grade practices**, making it ideal for college projects, hackathons, and resume-worthy system design discussions.

---

## ✨ Core Features

### 👤 User Management
- Student, Alumni, and Professor roles
- JWT based login using email & password
- OAuth2 login (Google, LinkedIn)
- Profile completion with skills, interests, experience

### 🧑‍🏫 Mentorship System
- Students can request alumni as mentors
- Chat functionality is available between mentor and mentee.
- Alumni can accept or reject mentorship
- Professors can act as moderators/proctors

### 📝 Alumni Blogs
- Alumni can write experience-based blog posts
- Students can like, comment, and engage
- Feed ranking based on engagement like LinkedIn/Reddit

### 🔍 Smart Alumni Search
- Search alumni by:
  - Company
  - Skills
  - Domain
  - Graduation year
- AI-powered semantic search (RAG-based)

### 📄 Resume Builder
- AI-assisted resume generation
- Uses user-provided API key
- Auto-fill details, skills from AlumX profile

---

## 🤖 AI-Powered Capabilities

- **RAG-based Alumni Discovery**
  - Vector embeddings for skills & interests
  - Semantic similarity search
- **AI Resume Assistant**
  - Resume suggestions & formatting
- **Smart Search**
  - Natural language queries for alumni search

---

## 🏗 System Architecture

```
Client (Jetpack Compose)
        |
        v
Spring Boot API Gateway
        |
------------------------------------------------
| Auth | User | Blog | Mentor | Search | AI |
------------------------------------------------
        |
 PostgreSQL | Redis | Vector DB | Object Storage
```

---

## ⚡ Tech Stack

### Backend
- **Spring Boot 4**
- **Spring Security + OAuth2**
- **Spring Data JPA**
- **Hibernate**

### Databases
- **MySQL** – Primary database
- **Redis** – Caching & session management
- **Vector DB (Pinecone / Weaviate / FAISS)** – AI search

### AI & Search
- OpenAI / Gemini / HuggingFace APIs
- LangChain / Spring AI

### Infrastructure
- Docker
- GitHub Actions (CI/CD)
- AWS / GCP (optional)

---

## 🗄 Database Schema (High Level)

### Users
- id
- name
- email
- role (STUDENT / ALUMNI / PROFESSOR)
- skills
- interests
- company
- graduation_year

### Blogs
- id
- author_id
- content
- likes
- created_at

### Mentorship Requests
- id
- student_id
- alumni_id
- status (PENDING / ACCEPTED / REJECTED)

### Comments
- id
- blog_id
- user_id
- content

---

## 🔗 API Design

- RESTful APIs
- JWT-based authentication
- Role-based authorization
- Swagger/OpenAPI documentation

Example:
```
POST   /api/auth/login
GET    /api/users/search
POST   /api/mentorship/request
GET    /api/blogs/feed
```

---

## 🔐 Security

- JWT authentication
- OAuth2 login
- Role-based access control (RBAC)
- Input validation & sanitization
- Rate limiting (Redis)

---

## 🛠️ Project Setup

### Prerequisites
- Java 17+
- Maven
- PostgreSQL
- Redis

### Setup Steps

```bash
git clone https://github.com/opencodeiiita/alum-x-backend.git
cd alum-x-backend
```

```bash
cp application.yml.example application.yml
```

```bash
mvn clean install
mvn spring-boot:run
```

Server runs at:
```
http://localhost:8080
```

---

## 📁 Directory Structure

```
src/main/java/com/alumx
├── auth
├── user
├── blog
├── mentorship
├── search
├── ai
├── config
├── security
└── common
```

---

## 🤝 Contribution Guidelines

- Follow clean architecture
- Use meaningful commit messages
- Open PRs with proper descriptions
- Avoid pushing secrets

---

## 🚀 Future Enhancements

- Real-time chat (WebSockets)
- Alumni referral system
- Analytics dashboard
- Job & internship postings
- Mobile push notifications
