# 01Blog - Angular Frontend

A complete, production-ready Angular frontend application for a social blogging platform with authentication, post management, comments, subscriptions, notifications, and admin features.

## Features

### Authentication
- User registration with email, username, and password
- Login with JWT token-based authentication
- Form validation with real-time feedback
- Secure token storage and HTTP interceptors

### Main Layout
- Responsive sidebar navigation
- Top navigation bar with notifications badge
- User profile menu with quick actions
- Material Design components throughout

### Home Feed
- Display posts from subscribed users
- Infinite scroll with pagination
- Like/unlike posts
- Comment on posts
- Real-time interaction updates

### User Profile (Block)
- View user profile with avatar, bio, and posts
- Subscribe/unsubscribe to users
- Create, edit, and delete own posts
- Post creation with text and media (image/video)
- Media preview before posting
- Report user functionality

### Posts & Comments
- Create posts with text and optional media
- Like/unlike posts with live count updates
- Comment system with CRUD operations
- Real-time comment updates
- Post ownership verification for edit/delete

### Notifications
- Real-time notification polling (30-second intervals)
- Notification badge with unread count
- Mark individual notifications as read
- Mark all notifications as read
- Notification types: likes, comments, subscriptions

### Reports
- Report users with detailed reasons
- Form validation for report submissions
- Confirmation dialog before submission

### Admin Dashboard
- View all users, posts, and reports
- Material tables with sorting
- Ban users
- Delete posts
- Update report status (Pending → Reviewed → Resolved)
- Admin-only access with guard protection

### Settings
- Update profile information
- Change username, full name, bio, and avatar
- Form validation
- Real-time updates

## Tech Stack

- **Framework**: Angular 20
- **UI Library**: Angular Material 20
- **State Management**: RxJS with Services
- **HTTP Client**: Angular HttpClient with Interceptors
- **Routing**: Angular Router with Guards
- **Forms**: Reactive Forms with Validators
- **Authentication**: JWT with localStorage
- **Architecture**: Standalone Components

## Project Structure

```
src/
├── app/
│   ├── core/                          # Core services, models, guards
│   │   ├── guards/
│   │   │   ├── auth.guard.ts         # Authentication guard
│   │   │   └── admin.guard.ts        # Admin role guard
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts   # JWT token interceptor
│   │   ├── models/
│   │   │   ├── user.model.ts         # User interfaces
│   │   │   ├── post.model.ts         # Post interfaces
│   │   │   ├── comment.model.ts      # Comment interfaces
│   │   │   ├── notification.model.ts # Notification interfaces
│   │   │   └── report.model.ts       # Report interfaces
│   │   └── services/
│   │       ├── auth.service.ts       # Authentication service
│   │       ├── user.service.ts       # User management
│   │       ├── post.service.ts       # Post CRUD operations
│   │       ├── comment.service.ts    # Comment operations
│   │       ├── notification.service.ts # Notification polling
│   │       ├── report.service.ts     # Report management
│   │       └── admin.service.ts      # Admin operations
│   ├── features/
│   │   ├── auth/
│   │   │   ├── login/               # Login component
│   │   │   └── register/            # Registration component
│   │   ├── layout/                  # Main layout with sidebar
│   │   ├── feed/                    # Home feed component
│   │   ├── profile/                 # User profile component
│   │   ├── post/
│   │   │   ├── create-post-dialog/  # Post creation dialog
│   │   │   └── comment-dialog/      # Comments dialog
│   │   ├── notifications/           # Notifications page
│   │   ├── report/
│   │   │   └── report-dialog/       # User report dialog
│   │   ├── admin/                   # Admin dashboard
│   │   └── settings/                # User settings
│   ├── shared/
│   │   └── components/
│   │       ├── post-card/           # Reusable post card
│   │       └── comment-list/        # Reusable comment list
│   └── app.routes.ts                # Application routing
├── environments/
│   ├── environment.ts               # Development config
│   └── environment.prod.ts          # Production config
└── main.ts                          # Application bootstrap

```

## API Integration

The application expects a REST API with the following endpoints:

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Users
- `GET /api/users/:id` - Get user profile
- `PUT /api/users/profile` - Update own profile
- `POST /api/subscriptions/:userId` - Subscribe to user
- `DELETE /api/subscriptions/:userId` - Unsubscribe from user
- `GET /api/subscriptions/:userId/status` - Check subscription status

### Posts
- `GET /api/posts/feed?page=1&limit=10` - Get feed posts
- `GET /api/posts/user/:userId?page=1&limit=10` - Get user posts
- `GET /api/posts/:id` - Get single post
- `POST /api/posts` - Create post
- `PUT /api/posts/:id` - Update post
- `DELETE /api/posts/:id` - Delete post
- `POST /api/posts/:id/like` - Like post
- `DELETE /api/posts/:id/like` - Unlike post

### Comments
- `GET /api/posts/:postId/comments` - Get post comments
- `POST /api/posts/:postId/comments` - Create comment
- `DELETE /api/posts/:postId/comments/:commentId` - Delete comment

### Notifications
- `GET /api/notifications` - Get all notifications
- `PATCH /api/notifications/:id/read` - Mark as read
- `PATCH /api/notifications/read-all` - Mark all as read

### Reports
- `POST /api/reports` - Create report
- `GET /api/admin/reports` - Get all reports (admin)
- `PATCH /api/admin/reports/:id` - Update report status (admin)

### Admin
- `GET /api/admin/users` - Get all users
- `GET /api/admin/posts` - Get all posts
- `POST /api/admin/users/:id/ban` - Ban user
- `DELETE /api/admin/posts/:id` - Delete post

## Installation & Setup

### Prerequisites
- Node.js 18+ and npm 9+
- Backend API running (see API Integration section)

### Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd 01blog-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment**

   Update `src/environments/environment.ts` with your backend API URL:
   ```typescript
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:3000/api'  // Your backend URL
   };
   ```

4. **Run development server**
   ```bash
   npm start
   ```

   The application will be available at `http://localhost:4200`

5. **Build for production**
   ```bash
   npm run build
   ```

   Production files will be in `dist/demo/`

## Development

### Running the app
```bash
npm start
```

### Building
```bash
npm run build
```

### Code Organization

- **Standalone Components**: All components are standalone for better tree-shaking
- **Lazy Loading**: Routes are lazy-loaded for optimal performance
- **Guards**: Auth and admin guards protect routes
- **Interceptors**: HTTP interceptor automatically adds JWT tokens
- **Services**: Centralized business logic in injectable services
- **Models**: TypeScript interfaces for type safety

### Best Practices Followed

1. **Single Responsibility**: Each component/service has one clear purpose
2. **DRY Principle**: Reusable components (post-card, comment-list)
3. **Type Safety**: Full TypeScript typing throughout
4. **Error Handling**: Comprehensive error handling with user feedback
5. **Responsive Design**: Mobile-friendly Material Design
6. **Security**: JWT authentication, role-based access control
7. **Performance**: Lazy loading, efficient change detection

## Key Components

### AuthService
Manages authentication state, login, register, and token storage.

### Guards
- `authGuard`: Protects routes requiring authentication
- `adminGuard`: Protects admin-only routes

### Interceptors
- `authInterceptor`: Automatically adds JWT token to HTTP requests

### Material Components Used
- MatCard, MatButton, MatIcon
- MatFormField, MatInput
- MatList, MatTable
- MatDialog, MatSnackBar
- MatSidenav, MatToolbar
- MatBadge, MatMenu
- MatTabs, MatProgressSpinner

## Configuration

### API URL
Change the API URL in `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://your-api-url/api'
};
```

### Notification Polling Interval
Adjust polling interval in `layout.component.ts`:
```typescript
this.notificationService.startPolling(30000); // 30 seconds
```

## User Roles

### USER (default)
- View feed from subscribed users
- Create, edit, delete own posts
- Like and comment on posts
- Subscribe to other users
- View notifications
- Report users
- Update profile settings

### ADMIN
- All USER permissions
- Access admin dashboard
- View all users, posts, and reports
- Ban users
- Delete any post
- Manage reports

## Security Features

1. **JWT Authentication**: Secure token-based authentication
2. **HTTP Interceptor**: Automatic token attachment
3. **Route Guards**: Protect routes based on auth and role
4. **Local Storage**: Secure token and user data storage
5. **Form Validation**: Client-side validation for all forms
6. **CORS Ready**: Configured for cross-origin requests

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Issue: "Cannot find module" errors
**Solution**: Run `npm install --include=dev` to ensure dev dependencies are installed.

### Issue: API requests failing
**Solution**: Check CORS configuration on backend and verify API URL in environment files.

### Issue: Build fails
**Solution**: Clear node_modules and reinstall:
```bash
rm -rf node_modules package-lock.json
npm install
```

### Issue: Notifications not updating
**Solution**: Check that the notification service polling is started in the layout component.

## License

MIT

## Author

Generated with Angular best practices and Material Design guidelines.
