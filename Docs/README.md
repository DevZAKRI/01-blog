# 📝 ZeroOneBlog - Social Blogging Platform

A modern, full-stack social blogging platform designed for students. Built with **Spring Boot 3.5** and **Angular 20**, featuring JWT authentication, rich text editing, real-time notifications, and admin moderation tools.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)
![Angular](https://img.shields.io/badge/Angular-20-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Running the Backend](#-running-the-backend)
- [Running the Frontend](#-running-the-frontend)
- [Docker Deployment](#-docker-deployment)
- [API Endpoints](#-api-endpoints)
- [Environment Configuration](#-environment-configuration)

---

## ✨ Features

### User Features
- 🔐 **Authentication** - Register, login with email/username, JWT-based sessions
- 📝 **Rich Text Posts** - Create posts with Quill editor, support for images (up to 4)
- 💬 **Comments** - Nested comment system with likes
- ❤️ **Likes** - Like posts and comments
- 🔔 **Notifications** - Real-time notifications for follows, likes, comments
- 👤 **Profiles** - User profiles with bio, avatar, post history
- 🔍 **Explore** - Discover new content and users
- 🔔 **Subscriptions** - Follow users and get feed updates

### Admin Features
- 🛡️ **User Management** - Ban/unban users
- 📊 **Content Moderation** - Hide/show posts
- 🚨 **Report System** - Review and manage user reports

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.5.7 | Application framework |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.x | Data persistence |
| **Hibernate** | 6.x | ORM |
| **PostgreSQL** | 15 | Primary database |
| **H2** | - | In-memory testing database |
| **JWT (jjwt)** | 0.11.5 | Token-based authentication |
| **Lombok** | - | Boilerplate code reduction |
| **Maven** | 3.x | Build tool |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Angular** | 20 | Frontend framework |
| **Angular Material** | 20.x | UI component library |
| **Angular CDK** | 20.x | Component development kit |
| **RxJS** | 7.8 | Reactive programming |
| **Quill / ngx-quill** | 2.0 / 26.0 | Rich text editor |
| **TypeScript** | 5.8 | Programming language |

### DevOps & Infrastructure
| Technology | Purpose |
|------------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **Nginx** | Frontend web server & reverse proxy |
| **Embedded Tomcat** | Backend application server |

---

## 📁 Project Structure

```
01blog/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/zerooneblog/blog/
│   │   ├── config/            # Security, CORS, JWT configs
│   │   ├── controller/        # REST controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Custom exceptions
│   │   ├── mapper/            # Entity ↔ DTO mappers
│   │   ├── model/             # JPA entities
│   │   ├── repository/        # Spring Data repositories
│   │   ├── service/           # Business logic
│   │   └── util/              # Utilities (JWT, Sanitizer)
│   ├── src/main/resources/
│   │   └── application.yaml   # App configuration
│   ├── uploads/               # User uploaded files
│   ├── pom.xml                # Maven dependencies
│   └── Dockerfile
│
├── frontend/                   # Angular SPA
│   ├── src/app/
│   │   ├── core/              # Guards, interceptors, services, models
│   │   ├── features/          # Feature modules (auth, feed, profile, etc.)
│   │   └── shared/            # Shared components
│   ├── src/environments/      # Environment configs
│   ├── angular.json           # Angular CLI config
│   ├── package.json           # NPM dependencies
│   └── Dockerfile
│
├── Docs/                       # Documentation
├── docker-compose.yml          # Full stack deployment
└── Deploy.sh                   # Deployment script
```

---

## 📦 Prerequisites

### For Local Development
- **Java 17+** - [Download](https://adoptium.net/)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **PostgreSQL 15+** - [Download](https://www.postgresql.org/download/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)

### For Docker Deployment
- **Docker** - [Install](https://docs.docker.com/get-docker/)
- **Docker Compose** - [Install](https://docs.docker.com/compose/install/)

---

## 🚀 Quick Start

### Option 1: Docker (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd 01blog

# Start all services
docker-compose up -d

# Access the application
# Frontend: http://localhost
# Backend API: http://localhost:8080/api/v1
```

### Option 2: Local Development

```bash
# Terminal 1 - Start PostgreSQL (or use Docker)
docker run -d --name blog_db \
  -e POSTGRES_DB=blog_db \
  -e POSTGRES_USER=ZAKRI \
  -e POSTGRES_PASSWORD='jw52U[6^K/8v' \
  -p 5432:5432 \
  postgres:15-alpine

# Terminal 2 - Start Backend
cd backend
./mvnw spring-boot:run

# Terminal 3 - Start Frontend
cd frontend
npm install
npm start

# Access the application
# Frontend: http://localhost:4200
# Backend API: http://localhost:8080/api/v1
```

---

## ⚙️ Running the Backend

### 1. Configure Database

Edit `backend/src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blog_db
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update  # Creates/updates tables automatically
```

### 2. Build and Run

```bash
cd backend

# Using Maven Wrapper
./mvnw clean install
./mvnw spring-boot:run

# Or using Maven directly
mvn clean install
mvn spring-boot:run

# Or run the JAR
./mvnw package -DskipTests
java -jar target/blog-0.0.1-SNAPSHOT.jar
```

### 3. Verify Backend is Running

```bash
# Health check
curl http://localhost:8080/api/v1/posts

# Should return [] or list of posts
```

### Backend Profiles

```bash
# Development (default)
./mvnw spring-boot:run

# Production
./mvnw spring-boot:run -Dspring.profiles.active=prod

# With custom properties
./mvnw spring-boot:run -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mydb
```

---

## 🌐 Running the Frontend

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure API URL

Edit `frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

### 3. Start Development Server

```bash
# Development server with hot reload
npm start
# or
ng serve

# With specific port
ng serve --port 4200

# Open to network
ng serve --host 0.0.0.0
```

### 4. Build for Production

```bash
# Production build
npm run build
# or
ng build --configuration production

# Output in: frontend/dist/
```

### 5. Access the Application

Open your browser and navigate to: **http://localhost:4200**

---

## 🐳 Docker Deployment

### Using Docker Compose (Full Stack)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Rebuild after changes
docker-compose up -d --build
```

### Services Overview

| Service | Container | Port | Description |
|---------|-----------|------|-------------|
| **postgres** | blog_db | 5432 | PostgreSQL database |
| **backend** | blog_backend | 8080 | Spring Boot API |
| **frontend** | blog_frontend | 80 | Angular + Nginx |

### Environment Variables

Create a `.env` file in the root directory:

```env
# Database
DB_NAME=blogdb
DB_USER=bloguser
DB_PASS=blogpass
DB_PORT=5432

# JWT
JWT_SECRET=your-super-secret-jwt-key-minimum-64-characters-long-for-security
```

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login user |

### Posts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/posts` | Get all posts (paginated) |
| GET | `/api/v1/posts/{id}` | Get single post |
| POST | `/api/v1/posts` | Create post (auth required) |
| PUT | `/api/v1/posts/{id}` | Update post (auth required) |
| DELETE | `/api/v1/posts/{id}` | Delete post (auth required) |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/{id}` | Get user profile |
| PUT | `/api/v1/users/{id}` | Update profile (auth required) |
| POST | `/api/v1/users/{id}/subscribe` | Subscribe to user |
| DELETE | `/api/v1/users/{id}/subscribe` | Unsubscribe |

### Comments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/posts/{postId}/comments` | Get post comments |
| POST | `/api/v1/posts/{postId}/comments` | Add comment |
| DELETE | `/api/v1/comments/{id}` | Delete comment |

### Feed & Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/feed` | Get user feed (auth required) |
| GET | `/api/v1/notifications` | Get notifications |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/users` | List all users |
| PUT | `/api/v1/admin/users/{id}/ban` | Ban user |
| PUT | `/api/v1/admin/posts/{id}/hide` | Hide post |
| GET | `/api/v1/admin/reports` | View reports |

---

## ⚙️ Environment Configuration

### Backend (application.yaml)

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/blog_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASS:password}
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:update}
    show-sql: ${SHOW_SQL:false}
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 100MB

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000  # 24 hours

server:
  port: ${PORT:8080}
```

### Frontend (environment.ts)

```typescript
// Development
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};

// Production (environment.prod.ts)
export const environment = {
  production: true,
  apiUrl: '/api/v1'  // Proxied through Nginx
};
```

---

## 🧪 Testing

### Backend Tests

```bash
cd backend

# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Skip tests during build
./mvnw package -DskipTests
```

### Frontend Tests

```bash
cd frontend

# Unit tests
npm test

# E2E tests
npm run e2e
```

---

## 📝 License

This project is for educational purposes - A social blogging platform for students.

---

## 👨‍💻 Author

**ZAKRI** - Full Stack Developer

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
