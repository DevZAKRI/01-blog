# Backend Logging Guide

## Overview
Comprehensive logging has been added to all CRUD operations in the backend to help track requests, identify failures, and debug issues. All logs use Java's `java.util.logging.Logger` with a consistent format.

## Logging Format
All logs follow this naming convention: `[ClassName] Operation - Message`

Examples:
- `[UserController] GET /users/{id} - Fetching user with id: 5`
- `[PostService] create() - Step 1: Checking if author is banned`
- `[NotificationController] Error listing notifications: Post not found`

## Controllers with Logging

### UserController
**Endpoint:** `/api/v1/users`

Logs CRUD operations:
- **GET /users/{id}** - Fetching single user profile
  - Logs: User ID requested, user found confirmation
  - Error: User not found

- **GET /users** - List all users (paginated)
  - Logs: Page and size parameters, total elements, current page count
  - Error: Error listing users

- **POST /users/{id}/subscribe** - Subscribe to a user
  - Logs: User subscribing, target user found, subscription confirmation
  - Error: Subscribe operation failed

- **POST /users/{id}/unsubscribe** - Unsubscribe from a user
  - Logs: User unsubscribing, target user found, unsubscription confirmation
  - Error: Unsubscribe operation failed

### PostController
**Endpoint:** `/api/v1/posts`

Logs CRUD operations:
- **POST /posts** - Create a new post
  - Logs: Step 1-8 of creation process
  - Step 1: Starting post creation, auth name
  - Step 2: Current user retrieved
  - Step 3-4: Post object created, media URLs serialized
  - Step 5-6: Post saved, notification sent
  - Step 7-8: DTO conversion, success confirmation
  - Error: Exception details at each step

- **PUT /posts/{id}** - Edit a post
  - Logs: Post ID, authorization check
  - Error: Post not found, user not authorized

- **DELETE /posts/{id}** - Delete a post
  - Logs: Post ID deletion
  - Error: Post not found, user not authorized

- **GET /posts/{id}** - Get single post
  - Logs: Post ID fetch
  - Error: Post not found

### CommentController
**Endpoint:** `/api/v1/posts/{postId}/comments`

Logs CRUD operations:
- **POST /comments** - Add comment to post
  - Logs: Step 1-6 of creation process
  - Step 1: Starting comment creation, post ID
  - Step 2: Current user retrieved
  - Step 3-4: Comment object created, saved to database
  - Step 5-6: DTO conversion, post author notification
  - Error: Exception details at each step

- **GET /comments** - List comments for a post
  - Logs: Post ID, pagination parameters, total comments found
  - Error: Post not found, error listing comments

- **DELETE /comments/{commentId}** - Delete comment
  - Logs: Comment ID, post ID, success confirmation
  - Error: Comment not found, user not authorized

- **POST /comments/{commentId}/like** - Like a comment
  - Logs: Comment ID, success confirmation
  - Error: Error liking comment

### NotificationController
**Endpoint:** `/api/v1/notifications`

Logs notification operations:
- **GET /notifications** - List user notifications
  - Logs: Pagination parameters, total notifications, no notifications message
  - Error: Error listing notifications

- **POST /notifications/{id}/read** - Mark notification as read
  - Logs: Notification ID, success confirmation
  - Error: Notification not found, user not authorized

- **POST /notifications/{id}/unread** - Mark notification as unread
  - Logs: Notification ID, success confirmation
  - Error: Notification not found, user not authorized

- **GET /notifications/unread-count** - Get unread count
  - Logs: Unread count value
  - Error: Error getting unread count

## Services with Logging

### PostService
Logs business logic:
- **create()** - Create post
  - Logs: Author ban check, post save, subscriber notification count
  - Error: Author banned, serialization failures

- **edit()** - Edit post
  - Logs: Post ID, authorization check, edit success
  - Error: Post not found, user not authorized

- **delete()** - Delete post
  - Logs: Post ID, authorization check, deletion success
  - Error: Post not found, user not authorized

- **getById()** - Fetch post
  - Logs: Post ID
  - Error: Post not found

### CommentService
Logs business logic:
- **addComment()** - Add comment
  - Logs: Steps 1-7 including post lookup, comment save, author notification
  - Error: Post not found, notification send failures

- **listComments()** - List comments
  - Logs: Post ID, total comments found
  - Error: Post not found

- **deleteComment()** - Delete comment
  - Logs: Comment and post ID, authorization check, deletion success
  - Error: Comment not found, authorization failures

### NotificationService
Logs business logic:
- **createNotification()** - Create notification
  - Logs: Receiver username, type, notification ID created
  - Error: Creation failures

- **list()** - List notifications
  - Logs: Receiver username, total notifications found
  - Error: Listing failures

- **markRead()** - Mark notification as read/unread
  - Logs: Notification ID, read status, success confirmation
  - Error: Notification not found, authorization failures

- **countUnread()** - Count unread
  - Logs: Receiver username, unread count
  - Error: Counting failures

- **markAllRead()** - Mark all as read
  - Logs: Receiver username, count marked as read
  - Error: Marking failures

## Log Levels Used

- **INFO** (Level.INFO): Normal operations, successes, important state changes
  - Used for: Starting operations, successful completions, status changes

- **FINE** (Level.FINE): Detailed operational information
  - Used for: Intermediate steps, parameter values, detailed processing info

- **SEVERE** (Level.SEVERE): Error conditions
  - Used for: Failures, exceptions, authorization failures

## How to View Logs

### During Development (Spring Boot)
Logs appear in the console output where you run `mvn spring-boot:run`.

Example output:
```
2026-01-11T20:30:00.000+01:00 INFO 12345 --- [main] [UserController] GET /users/{id} - Fetching user with id: 1
2026-01-11T20:30:00.001+01:00 INFO 12345 --- [main] [UserController] User found: testuser
2026-01-11T20:30:00.005+01:00 INFO 12345 --- [main] [PostController] POST /posts - Step 1: Starting post creation
2026-01-11T20:30:00.006+01:00 INFO 12345 --- [main] [PostController] Auth name: testuser@example.com
```

### Configuration
To change log levels, edit `application.yaml`:

```yaml
logging:
  level:
    com.zerooneblog.blog.controller: DEBUG  # or INFO, FINE, SEVERE
    com.zerooneblog.blog.service: DEBUG
```

## Troubleshooting Guide

### Issue: GET /api/v1/users returns 500 error

**Steps to debug using logs:**

1. Look for: `[UserController] GET /users - Listing users`
   - If missing: Request never reached controller

2. Look for: `[UserService] listAll()` logs
   - If missing: Issue in repository call
   - If error: Check database connection

3. Look for: `[UserController] Error listing users: ...`
   - Check the error message for specific failure

### Issue: POST /api/v1/posts fails

**Steps to debug using logs:**

1. Look for: `[PostController] POST /posts - Step 1: Starting post creation`
   - If missing: Validation error before controller

2. Look for: `[PostController] Step 2: Getting current user`
   - If missing/error: Authentication issue

3. Look for: `[PostService] create() - Step 1: Checking if author is banned`
   - Verify author is not banned

4. Look for: `[PostService] create() - Step 3: Post saved with ID:`
   - Confirms database save success

5. Look for: `[PostService] create() - Step 4: Notifying subscribers`
   - Check notification sending

### Issue: Notifications not appearing

**Steps to debug using logs:**

1. Look for: `[CommentService] addComment() - Step 6: Notifying post author`
   - Check for errors in this step

2. Look for: `[NotificationService] createNotification() - Notification created with ID:`
   - Confirms notification was created

3. Look for: `[NotificationController] GET /notifications - Listing notifications`
   - Verify notifications are retrieved

4. Check if query shows: `[NotificationController] Found X total notifications`
   - Confirms notifications exist in database

## Summary

All major CRUD operations now have comprehensive logging that tracks:
- When operations start and which user/entity is involved
- Step-by-step progress through the operation
- Parameter values and intermediate results
- Success/failure status
- Specific error messages and exceptions

This makes debugging issues much easier as you can follow the exact execution path and identify where failures occur.
