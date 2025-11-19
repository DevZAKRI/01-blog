# 01Blog — Backend

This README documents the backend for the 01Blog project (Spring Boot application). It covers the REST API surface, DTO conventions, how to run the service locally, and a short explanation of how requests flow through Spring / Spring Boot down to kernel-level networking.

## Quick start

Requirements:
- Java 17
- Maven (the project includes the Maven wrapper `mvnw`)

Run locally from the `backend/` folder:

```bash
# Build (skip tests for speed):
./mvnw -DskipTests package

# Run (jar produced in target/):
java -jar target/blog-0.0.1-SNAPSHOT.jar
```

By default the app uses the configuration in `src/main/resources/application.yaml`. For production you should set environment variables or provide an external `application.yaml`.

## High level architecture

- Layered design: Controller → Service → Repository → Database.
- DTO separation:
  - `com.zerooneblog.blog.dto.request` — incoming request shapes (validated with Jakarta Validation annotations).
  - `com.zerooneblog.blog.dto.response` — outgoing response shapes.
  - `mapper/EntityMapper` converts entities to response DTOs.
- Security: JWT-based auth (existing `JwtUtil` and `JwtAuthenticationFilter`), password hashing with `PasswordEncoder` (BCrypt).
- File uploads: stored under `uploads/` and served via a static resource mapping `/uploads/**`.
- Notifications persisted in `notifications` table and delivered via REST endpoints (stored, not pushed — front-end polls or uses web sockets if you extend the app).

## REST API (summary)

All endpoints are prefixed with `/api/v1`.

Auth
- POST `/api/v1/auth/register` — Register. Request: `dto.request.RegisterRequest`. Response: `dto.response.AuthResponse` (token).
- POST `/api/v1/auth/login` — Login. Request: `dto.request.LoginRequest`. Response: `dto.response.AuthResponse` (token).

Users
- GET `/api/v1/users` — List users (paginated). Response: `Page<UserDto>`
- GET `/api/v1/users/{id}` — Public profile. Response: `UserDto`
- POST `/api/v1/users/{id}/subscribe` — Subscribe to a user (auth required).
- POST `/api/v1/users/{id}/unsubscribe` — Unsubscribe (auth required).
- PUT `/api/v1/users/me` — Update current user's profile. Request: `dto.request.UpdateUserRequest`.
- GET `/api/v1/users/{authorId}/posts` — List posts for subscribed author (auth required; checks subscription).

Posts & feed
- POST `/api/v1/posts` — Create post (auth). Request: `dto.request.CreatePostRequest`. Response: `PostDto`.
- PUT `/api/v1/posts/{id}` — Edit post (auth). Request: `dto.request.UpdatePostRequest`.
- DELETE `/api/v1/posts/{id}` — Delete post (auth).
- GET `/api/v1/posts/{id}` — Get post. Response: `PostDto`.
- GET `/api/v1/posts` — List posts (paginated).
- GET `/api/v1/feed` — Feed for current user (paginated).
- GET `/api/v1/feed/{authorId}` — Feed for specific author (paginated, requires subscription to that author).

Comments
- POST `/api/v1/posts/{postId}/comments` — Add comment (auth). Request: `dto.request.CreateCommentRequest`. Response: `CommentDto`.
- GET `/api/v1/posts/{postId}/comments` — List comments (paginated).

Likes
- POST `/api/v1/posts/{id}/like` — Like post (auth).
- DELETE `/api/v1/posts/{id}/like` — Unlike post (auth).

Reports (user reporting)
- POST `/api/v1/users/{id}/report` — Report user. Request: `dto.request.ReportRequest`. Response: `ReportDto`.

Notifications
- GET `/api/v1/notifications` — List notifications (paginated). Response: `Page<NotificationDto>`
- POST `/api/v1/notifications/{id}/read` — Mark one read (auth, only owner allowed).
- POST `/api/v1/notifications/{id}/unread` — Mark one unread.
- GET `/api/v1/notifications/unread-count` — Get unread count.
- POST `/api/v1/notifications/mark-all-read` — Mark all as read.

Admin
- All admin endpoints are protected by `ROLE_ADMIN`.
- GET `/api/v1/admin/users` — Paginated list of users.
- DELETE `/api/v1/admin/users/{id}` — Delete user.
- GET `/api/v1/admin/posts` — Paginated posts.
- DELETE `/api/v1/admin/posts/{id}` — Delete post.
- GET `/api/v1/admin/reports` — Paginated reports.

Uploads
- POST `/api/v1/uploads` — Multipart file upload (multipart/form-data) with `file` field. Returns JSON `{ "path": "/uploads/.." }`.

Notes on pagination: controllers accept `page` and `size` query params and return Spring `Page<T>` which includes total counts and content.

## DTO conventions and validation

- Requests live in `dto.request` and include Jakarta Validation annotations (`@NotBlank`, `@Email`, etc.). Controller methods annotate `@Valid` on request bodies.
- Responses live in `dto.response` and are produced by `EntityMapper`.

## Code sweep & safety

I performed a sweep to ensure controllers return DTOs or `ResponseEntity` rather than domain entities. Services still return entities (internal layer) which is normal. If you want, I can convert service return types to DTOs to push mapping lower in the stack.

## How a request flows — short walkthrough (from client to kernel level)

This is a simplified overview of what happens when a client calls the backend (for example, `POST /api/v1/posts`):

1. Client issues an HTTP request (browser, curl, mobile app). The request is sent over TCP to the server address:port.

2. Kernel & networking stack (Linux):
   - The server process (the JVM running your Spring Boot app) has a listening socket bound to a port (for the embedded server, e.g., Tomcat/Jetty/Undertow inside Spring Boot).
   - The kernel receives TCP/IP packets, performs TCP reassembly, and queues fully formed connections/requests to the socket buffer for the JVM process.
   - The OS schedules the JVM process/thread to read from the socket and deliver bytes to the application.

3. Embedded servlet container (Spring Boot):
   - Spring Boot boots an embedded servlet container (commonly Tomcat). The container accepts the TCP stream and parses the HTTP request.
   - The container hands the request to the Servlet API implementation and calls the `DispatcherServlet` (Spring MVC main entry point) inside the JVM.

4. Filter chain & security
   - `Filter`s are executed in order (for example, CORS filter, logging filters, `JwtAuthenticationFilter`).
   - Authentication/authorization is handled by Spring Security: the JWT filter reads the `Authorization` header, validates the token (often by `JwtUtil`), and sets the `SecurityContext` with an authenticated `Authentication` object.

5. Controller invocation
   - `DispatcherServlet` maps the request to a controller method using handler mappings (controller `@RequestMapping` and method-level annotations).
   - Request body is deserialized to Java objects (Jackson for JSON).
   - Validation (`@Valid`) runs and will cause a 400 response if validation fails.
   - Method parameters like `Authentication` and `Principal` are resolved by argument resolvers.

6. Business logic & persistence
   - Controller delegates to service layer beans (annotated `@Service`). Services perform business logic and call Spring Data JPA repositories.
   - Repositories use JDBC drivers (e.g., PostgreSQL driver) to execute SQL. The JDBC driver communicates with the database over its own TCP connection. The database process is separate (Postgres server) and also uses kernel-level sockets.
   - Hibernate manages object-relational mapping, transactions, and caching inside the JVM.

7. Response
   - Controller returns a DTO. `DispatcherServlet` serializes the DTO to JSON (via Jackson) and writes bytes back to the servlet container.
   - The servlet container writes HTTP response bytes to the socket; the kernel sends TCP/IP packets back to the client.

8. Kernel scheduling & delivery
   - The kernel handles moving bytes to network hardware and the client receives the response. At no point does application code directly manipulate kernel buffers — it uses Java sockets and APIs provided by the JVM and servlet container.

Why this matters
- Understanding this helps diagnose performance and security problems: e.g., slow DB queries in services will stall controller responses; heavy serialization can block servlet threads; large file uploads should be streamed to avoid OOM.

## Development tips & next steps

- Add tests for critical flows (auth, post creation, subscription → notification).
- If you want OpenAPI / Swagger UI, I can add the Springdoc dependency and a small config (note: that requires a pom.xml change).
- Consider moving DTO mapping into the service layer if you want controllers to be thinner.

## Troubleshooting

- Build: use `./mvnw -DskipTests package` to build quickly.
- Run: `java -jar target/blog-0.0.1-SNAPSHOT.jar`.
- Logs: Spring Boot logs go to stdout. Use `--spring.profiles.active=...` or custom logging config for file-based logs.

If you'd like, I will (A) remove the deprecated DTO files now that the `dto.response` package is in place, and (B) run a final code sweep + tests. Say which you'd prefer and I'll proceed.
01Blog — Backend
=================

Overview
--------
This module is the Spring Boot backend for 01Blog — a social blogging platform. It provides REST APIs for users, posts, likes, comments, reports, notifications, media uploads, and an admin panel.

Tech stack
----------
- Java 17
- Spring Boot 3.5.x (Web, Data JPA, Security, Validation)
- Spring Security with JWT (project includes JWT utilities and filter)
- JPA (Hibernate) with runtime PostgreSQL (H2 available for tests)
- Lombok for concise data classes
- Maven build

Project layout (important parts)
-------------------------------
- `src/main/java/com/zerooneblog/blog/model` — JPA entities (User, Post, Comment, PostLike, Report, Notification)
- `.../repository` — Spring Data repositories
- `.../service` — Business logic (PostService, CommentService, LikeService, NotificationService, UserService, etc.)
- `.../controller` — REST controllers exposing API endpoints
- `.../dto` — API DTO classes (separate from entities)
- `.../mapper` — simple mapping utility between entities and DTOs
- `uploads/` — runtime folder where uploaded media are stored (created automatically)

Security
--------
Authentication is JWT-based. The existing `JwtAuthenticationFilter` integrates with Spring Security and the `CustomUserDetailsService` to load users.

Roles
- `ROLE_USER` (default)
- `ROLE_ADMIN` — can delete users/posts and manage admin operations

Key API endpoints (examples + shapes)
-----------------------------------
All endpoints are prefixed with `/api/v1`.

Example JSON shapes (request / response)

- RegisterRequest (POST /api/v1/auth/register)

```json
{
   "username": "alice",
   "password": "s3cret123",
   "email": "alice@example.com"
}
```

- AuthResponse (successful login/register)

```json
{
   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI..."
}
```

- CreatePostRequest (POST /api/v1/posts)

```json
{
   "description": "My first post",
   "mediaUrl": "/uploads/abc123.png"
}
```

- PostDto (response)

```json
{
   "id": 123,
   "authorId": 5,
   "authorUsername": "alice",
   "description": "My first post",
   "mediaUrl": "/uploads/abc123.png",
   "createdAt": "2025-11-19T12:34:56Z",
   "updatedAt": null
}
```

Auth & curl examples
--------------------
Register and login example using curl (returns a token):

```bash
# Register
curl -s -X POST http://localhost:8080/api/v1/auth/register \
   -H 'Content-Type: application/json' \
   -d '{"username":"alice","password":"s3cret123","email":"alice@example.com"}'

# Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
   -H 'Content-Type: application/json' \
   -d '{"username":"alice","password":"s3cret123"}'

# Use token for authorized requests (replace TOKEN)
curl -s -H "Authorization: Bearer TOKEN" http://localhost:8080/api/v1/feed
```

Notes on Authorization header
- The app expects `Authorization: Bearer <token>` for protected endpoints. Tokens are JWTs issued by `JwtUtil`.

Pagination example
------------------
Controllers accept `page` and `size` query parameters. Responses commonly return Spring `Page<T>` which serializes to JSON like:

```json
{
   "content": [ /* array of DTOs */ ],
   "pageable": { /* paging info */ },
   "totalElements": 123,
   "totalPages": 7,
   "last": false,
   "size": 20,
   "number": 0,
   "first": true
}
```

Error response shape (GlobalExceptionHandler)
-------------------------------------------
When controllers throw runtime exceptions, `GlobalExceptionHandler` returns a consistent error JSON (example):

```json
{
   "timestamp": "2025-11-19T12:34:56.789Z",
   "status": 404,
   "error": "Not Found",
   "message": "Post not found",
   "path": "/api/v1/posts/999"
}
```

Environment variables and configuration
---------------------------------------
The app reads Spring configuration from `application.yaml` and environment variables. Common environment variables you may want to set in production:

- `SPRING_DATASOURCE_URL` — JDBC URL for the database (e.g. `jdbc:postgresql://db:5432/blog`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` — secret used to sign JWTs (if your `JwtUtil` reads it from properties)
- `UPLOAD_DIR` — directory to store uploaded files (defaults to `./uploads`)
- `MAX_UPLOAD_SIZE` — max file upload size (bytes)
- `SPRING_PROFILES_ACTIVE` — e.g., `prod` / `dev`

Production tips
---------------
- Static uploads: store uploads on a persistent volume (or use S3) and serve them via a CDN or reverse proxy. Avoid storing uploads only in container writable layer.
- Run the app behind a reverse proxy (Nginx) for TLS termination, rate limiting, and better static file serving.
- Database migrations: add Flyway or Liquibase to manage schema changes in production.

CORS and security notes
-----------------------
- CORS is configured in `CorsConfig`. By default, allow only the origins you control. Avoid `*` in production.
- Spring Security roles: `@PreAuthorize("hasRole('ADMIN')")` checks for granted authority `ROLE_ADMIN`. When setting `User.role` string values ensure authorities are mapped correctly.

File uploads
------------
- The `FileStorageService` enforces a small MIME whitelist and size limit. For large files, implement streaming upload and consider chunked upload.

Logging & metrics
-----------------
- Spring Boot logs to stdout by default. For production, configure logback or log4j2 appenders to write to files or a log aggregation system.
- Consider adding metrics (Micrometer + Prometheus) and health checks (`spring-boot-starter-actuator`) for production readiness.

Health check
------------
- If you enable Spring Actuator, `/actuator/health` gives a quick liveness check. Add readiness probes in Kubernetes using that endpoint.

Troubleshooting & common issues
-------------------------------
- 401 on authenticated endpoints: ensure the `Authorization` header contains `Bearer <token>` and the token is valid.
- 403 on admin endpoints: check that the user has `ROLE_ADMIN` authority (roles often stored as `ROLE_ADMIN` in DB or mapped on login).
- File upload 413: increase `spring.servlet.multipart.max-file-size` in properties or enforce smaller files client-side.
- Token not being accepted: check clock skew (JWT expiration) and server time.

How to contribute
-----------------
- Follow the existing package structure. Keep DTOs separated from entities.
- Add tests for new features under `src/test/java`.
- If you want me to add Swagger/OpenAPI or tests, tell me and I'll implement them (note: Swagger requires a `pom.xml` dependency change).

Next recommended tasks I can implement for you
---------------------------------------------
1. Add Springdoc OpenAPI dependency and generate a Swagger UI and API YAML (requires `pom.xml` update).
2. Remove the deprecated DTO files left in `com.zerooneblog.blog.dto.deprecated` and run a final sweep.
3. Add a small set of integration tests for auth and post creation using the H2 profile.

Which of the above would you like me to do next? (or tell me other edits you want in the documentation)

