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
- (Removed) `ROLE_DATA_ANALYST` — previously allowed read-only admin access; this role is no longer used. Admin-only protections are enforced for admin endpoints.

Key API endpoints
-----------------
All endpoints are prefixed with `/api/v1`.

Users
- `GET /api/v1/users` — paginated list of users (ADMIN only)
- `GET /api/v1/users/{id}` — public user profile (User Block)
- `POST /api/v1/users/{id}/subscribe` — subscribe to a user (authenticated)
- `POST /api/v1/users/{id}/unsubscribe` — unsubscribe
- `GET /api/v1/users/{authorId}/posts` — list posts for a subscribed author (throws if not subscribed)

Posts
- `GET /api/v1/posts/{id}` — get post by id (public)
- `GET /api/v1/posts` — paginated list of posts
- `POST /api/v1/posts` — create post (authenticated)
- `PUT /api/v1/posts/{id}` — edit (author or ADMIN)
- `DELETE /api/v1/posts/{id}` — delete (author or ADMIN)

Feed
- `GET /api/v1/feed` — paginated feed (posts from all authors you subscribe to)
- `GET /api/v1/feed/{authorId}` — paginated feed for a single author you subscribe to

Likes & Comments
- `POST /api/v1/posts/{id}/like` — like a post
- `DELETE /api/v1/posts/{id}/like` — unlike a post
- `POST /api/v1/posts/{postId}/comments` — add a comment
- `GET /api/v1/posts/{postId}/comments` — paginated comments

Reports
- `POST /api/v1/users/{id}/report` — report a user (authenticated)

Notifications
- `GET /api/v1/notifications` — list notifications (paginated)
- `POST /api/v1/notifications/{id}/read` — mark notification as read
- `POST /api/v1/notifications/{id}/unread` — mark notification as unread

Uploads
- `POST /api/v1/uploads` — multipart upload for images/videos, returns stored path under `/uploads`.
  - Allowed MIME types: image/png, image/jpeg, image/jpg, image/gif, video/mp4, video/quicktime
  - Max file size: 10 MB

Admin panel
- `GET /api/v1/admin/users` — list users (ADMIN only)
- `DELETE /api/v1/admin/users/{id}` — delete/ban user (ADMIN only)
- `GET /api/v1/admin/posts` — list posts (ADMIN only)
- `DELETE /api/v1/admin/posts/{id}` — delete post (ADMIN only)
- `GET /api/v1/admin/reports` — list reports (ADMIN only)

Architecture & notes
--------------------
- Clean-ish separation: entities -> services -> controllers. DTOs + mapper provide separation between DB models and API contract.
- Notifications are created when:
  - an author publishes a post (subscribers get `new_post`)
  - a user comments on a post (post author gets `new_comment`)
  - when a user subscribes (subscriber should trigger `new_subscriber` — this can be wired in `UserService.subscribe` if desired)
Admin: `ADMIN` role is required for all admin endpoints. There is no `DATA_ANALYST` role in this codebase.

Build & Run
-----------
Requirements: Java 17, Maven

From project root:
```bash
cd backend
./mvnw spring-boot:run
```

To build a jar:
```bash
cd backend
./mvnw -DskipTests package
```

Testing
-------
- The project includes `spring-boot-starter-test`. Add tests under `src/test/java` for services and controllers. Integration tests may use H2 (already on the classpath).

Next improvements (suggested)
- Add DTOs for create/update requests and input validation (currently controllers accept entities or simple maps in places).
- Improve NotificationController to support marking a batch as read and real-time delivery (WebSocket or SSE).
- Add full OpenAPI/Swagger by including `springdoc-openapi` dependency and annotating controllers.
- Add unit and integration tests for critical flows (auth, posts, feed, uploads).
- Serve `uploads/` as static resources via Spring configuration or external CDN.

If you want, I can implement any of the suggested improvements next (pick one).
