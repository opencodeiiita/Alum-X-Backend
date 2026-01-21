
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

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- Docker
- VectorDB
- WebSockets

---

## Database Schema (High Level)

### Complete Database Diagram

View the complete, interactive database schema diagram here:

**[AlumX Database Diagram - Interactive View](https://dbdiagram.io/d/database-diagram-695381be39fa3db27bcae256)**

The diagram includes:
- **9 main entities**: User, Chat, Message, GroupChat, Participant, GroupMessage, JobPost, JobPostComment, JobPostLike, Resume
- **12 user profile collection tables**: skills, education, tech_stack, languages, frameworks, communication_skills, certifications, projects, soft_skills, hobbies, experience, internships
- **All relationships** properly mapped with foreign keys and cardinality (1:1, 1:N, N:M)
- **Indexes and constraints** as defined in the codebase

### Database Tables Overview

The AlumX backend uses the following main entity groups:

1. **User Management** - Core user profiles with role-based access (Student, Alumni, Professor)
2. **Chat System** - One-on-one messaging between users
3. **Group Chat** - Multi-user group conversations with participants
4. **Job Posts** - Alumni experience sharing with engagement features (likes, comments)
5. **Resume System** - AI-assisted resume management for users

All tables follow proper normalization principles and include appropriate indexes for optimal query performance.

---

## 🛠️ Project Setup
This project uses **Spring Boot + PostgreSQL** and reads configuration from **environment variables** to keep secrets out of the codebase.

### 1️⃣ Clone the repository

```bash
git clone https://github.com/opencodeiiita/alum-x-backend.git
cd alum-x-backend
```

---

### 2️⃣ Set up PostgreSQL locally

Make sure PostgreSQL is installed and running on your system.

#### Create database and user

Login to PostgreSQL as `postgres`:

```bash
psql -U postgres
```

Then run:

```sql
CREATE DATABASE alumx;
CREATE USER alumx WITH PASSWORD 'alumx123';
GRANT ALL PRIVILEGES ON DATABASE alumx TO alumx;
```

Exit PostgreSQL:

```sql
\q
```

---

### 3️⃣ Configure environment variables

The .env file you will need, will be provided to you by the mentor

> ⚠️ **Do not commit `.env`** — it is ignored via `.gitignore`.
---

### 4️⃣ IntelliJ Configuration

Spring Boot does **not** automatically load `.env` files in IntelliJ.

#### Steps:

1. Go to **Run → Edit Configurations**
2. Select your Spring Boot run configuration
3. Under **Environment variables**, click **Load from file**
4. Select the `.env` file
5. Apply and close

---

### 5️⃣ Build and run the backend

```bash
mvn clean install
mvn spring-boot:run
```

## 🤝 Contribution Guidelines

- Follow clean architecture
- Use meaningful commit messages
- Open PRs with proper descriptions
- Avoid pushing secrets

---
