# 🔄 Project Lifecycle - Under the Hood

A comprehensive deep-dive into the ZeroOneBlog architecture, explaining every layer from the browser to the database and back.

---

## 📋 Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Request Lifecycle - Complete Flow](#2-request-lifecycle---complete-flow)
3. [Frontend Layer (Angular)](#3-frontend-layer-angular)
4. [HTTP Communication](#4-http-communication)
5. [Backend Layer (Spring Boot)](#5-backend-layer-spring-boot)
6. [Security Layer](#6-security-layer)
7. [Service Layer](#7-service-layer)
8. [Data Access Layer](#8-data-access-layer)
9. [Database Layer](#9-database-layer)
10. [Memory Management](#10-memory-management)
11. [Complete Example: Creating a Post](#11-complete-example-creating-a-post)

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT BROWSER                                      │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                         ANGULAR APPLICATION                                 │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐   │ │
│  │  │  Components  │  │   Services   │  │    Guards    │  │ Interceptors  │   │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────────┘   │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │ HTTP/HTTPS (REST API)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SPRING BOOT SERVER                                  │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                           SECURITY FILTER CHAIN                             │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐   │ │
│  │  │ CORS Filter  │→ │ JWT Filter   │→ │ Auth Filter  │→ │Authorization  │   │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────────┘   │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                       │                                          │
│                                       ▼                                          │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                              CONTROLLER LAYER                               │ │
│  │     PostController │ UserController │ AuthController │ CommentController    │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                       │                                          │
│                                       ▼                                          │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                              SERVICE LAYER                                  │ │
│  │      PostService   │  UserService   │ NotificationService │ LikeService    │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                       │                                          │
│                                       ▼                                          │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                             REPOSITORY LAYER                                │ │
│  │   PostRepository  │ UserRepository │ CommentRepository │ LikeRepository     │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                       │                                          │
│                                       ▼                                          │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                            JPA / HIBERNATE                                  │ │
│  │        Entity Manager  │  Persistence Context  │  Query Translation         │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │ JDBC
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              POSTGRESQL DATABASE                                 │
│     users │ posts │ comments │ post_likes │ comment_likes │ notifications        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Request Lifecycle - Complete Flow

### What happens when you click "Create Post"?

```
USER ACTION                    TIME        MEMORY LOCATION
───────────────────────────────────────────────────────────────────────

1. Click "Post" button         0ms         Browser UI Thread
       │
       ▼
2. Angular Component           1ms         Browser Heap (JS)
   - Collect form data
   - Call PostService
       │
       ▼
3. HTTP Interceptor            2ms         Browser Heap (JS)
   - Attach JWT token
   - Set headers
       │
       ▼
4. HttpClient.post()           3ms         Browser Network Thread
   - Serialize to JSON
   - Send HTTP request
       │
       ▼
═══════════════════════════════════════════════════════════════════════
                        NETWORK (Internet)
═══════════════════════════════════════════════════════════════════════
       │
       ▼
5. Tomcat receives             50ms        JVM Stack (New Thread)
   - Parse HTTP request
   - Create HttpServletRequest
       │
       ▼
6. Security Filter Chain       51ms        JVM Heap (Filter beans)
   - CORS validation
   - JWT validation
   - Authentication
       │
       ▼
7. DispatcherServlet           52ms        JVM Heap (Singleton)
   - Route to controller
   - Argument resolution
       │
       ▼
8. PostController              53ms        JVM Heap (Singleton bean)
   - @RequestBody parsing
   - Validation
       │
       ▼
9. PostService                 54ms        JVM Heap (Singleton bean)
   - Business logic
   - Call repository
       │
       ▼
10. PostRepository             55ms        JVM Heap (Proxy)
    - JPA query generation
       │
       ▼
11. Hibernate/EntityManager    56ms        JVM Heap (Persistence Context)
    - SQL generation
    - Parameter binding
       │
       ▼
12. JDBC Connection            57ms        JVM Heap (Connection Pool)
    - Execute SQL
       │
       ▼
═══════════════════════════════════════════════════════════════════════
                        POSTGRESQL DATABASE
═══════════════════════════════════════════════════════════════════════
       │
       ▼
13. Query Execution            58-100ms    PostgreSQL Memory
    - Parse SQL
    - Query plan
    - Execute INSERT
    - Return result
       │
       ▼
═══════════════════════════════════════════════════════════════════════
                        RESPONSE PATH (reverse)
═══════════════════════════════════════════════════════════════════════
```

---

## 3. Frontend Layer (Angular)

### 3.1 Component Lifecycle

```typescript
// When PostDetailComponent loads
@Component({
  selector: 'app-post-detail',
  template: `...`
})
export class PostDetailComponent implements OnInit, OnDestroy {
  
  // 1. CONSTRUCTOR - Dependency Injection
  constructor(
    private postService: PostService,    // Injected singleton
    private route: ActivatedRoute        // Injected singleton
  ) {
    // Called when component class is instantiated
    // Services are injected here
    console.log('Constructor: Dependencies injected');
  }
  
  // 2. ngOnInit - Component Initialization
  ngOnInit(): void {
    // Called after Angular initializes component's views
    // Safe to access @Input() properties
    // Good place to fetch data
    this.loadPost();
  }
  
  // 3. ngOnDestroy - Cleanup
  ngOnDestroy(): void {
    // Called before component is destroyed
    // Unsubscribe from observables
    this.subscription.unsubscribe();
  }
}
```

### 3.2 Service Layer (Angular)

```typescript
@Injectable({
  providedIn: 'root'  // Singleton - same instance everywhere
})
export class PostService {
  
  constructor(private http: HttpClient) {}
  
  createPost(data: CreatePostRequest): Observable<Post> {
    // Returns Observable - lazy execution
    // HTTP call only happens when subscribed
    return this.http.post<Post>(`${environment.apiUrl}/posts`, data);
  }
}
```

### 3.3 HTTP Interceptor Chain

```
HTTP Request Flow:
                                                    
  Component                                         
      │                                             
      ▼                                             
  HttpClient.post()                                 
      │                                             
      ▼                                             
┌─────────────────┐                                
│ AuthInterceptor │  ← Adds Authorization header   
└────────┬────────┘                                
         │                                          
         ▼                                          
┌─────────────────┐                                
│ ErrorInterceptor│  ← Handles HTTP errors         
└────────┬────────┘                                
         │                                          
         ▼                                          
   HttpBackend                                      
      │                                             
      ▼                                             
   NETWORK ────────────────────────────────────►   
```

```typescript
// auth.interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    
    // 1. Get token from storage
    const token = localStorage.getItem('accessToken');
    
    // 2. Clone request and add header
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    // 3. Pass to next interceptor
    return next.handle(req);
  }
}
```

### 3.4 Guards (Route Protection)

```typescript
// auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  // Check if user is authenticated
  if (authService.isAuthenticated()) {
    return true;  // Allow navigation
  }
  
  // Redirect to login
  return router.createUrlTree(['/auth/login']);
};
```

```
Route Navigation Flow:

User clicks /profile/123
        │
        ▼
┌───────────────────┐
│   Router checks   │
│   canActivate     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐     NO      ┌──────────────┐
│   authGuard()     │───────────► │ Redirect to  │
│   Authenticated?  │             │   /login     │
└─────────┬─────────┘             └──────────────┘
          │ YES
          ▼
┌───────────────────┐
│  Load Component   │
│  ProfileComponent │
└───────────────────┘
```

---

## 4. HTTP Communication

### 4.1 Request Structure

```
POST /api/v1/posts HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGVtYWls...
Content-Length: 142

{
  "title": "My First Post",
  "description": "<p>Hello World!</p>",
  "mediaUrls": ["http://localhost:8080/uploads/image1.jpg"]
}
```

### 4.2 Response Structure

```
HTTP/1.1 201 Created
Content-Type: application/json
X-Content-Type-Options: nosniff
X-Frame-Options: DENY

{
  "id": 42,
  "title": "My First Post",
  "description": "<p>Hello World!</p>",
  "mediaUrls": ["http://localhost:8080/uploads/image1.jpg"],
  "author": {
    "id": 1,
    "username": "zakri",
    "avatarUrl": "/uploads/avatars/zakri.jpg"
  },
  "createdAt": "2026-01-12T10:30:00Z",
  "likeCount": 0,
  "commentCount": 0
}
```

---

## 5. Backend Layer (Spring Boot)

### 5.1 Application Startup

```
JVM Startup Sequence:
═══════════════════════════════════════════════════════════════════

1. JVM INITIALIZATION (0-500ms)
   ├── Load JRE classes
   ├── Initialize heap memory
   └── Start main thread

2. SPRING BOOT BOOTSTRAP (500-2000ms)
   ├── Load SpringApplication.run()
   ├── Create ApplicationContext
   └── Read application.yaml

3. COMPONENT SCANNING (2000-3000ms)
   ├── Scan @Component, @Service, @Repository, @Controller
   ├── Create BeanDefinitions
   └── Resolve dependencies

4. BEAN INSTANTIATION (3000-4000ms)
   ├── Create singleton beans
   ├── Inject dependencies
   └── Run @PostConstruct methods

5. AUTOCONFIGURATION (4000-5000ms)
   ├── DataSourceAutoConfiguration → Create connection pool
   ├── JpaAutoConfiguration → Create EntityManagerFactory
   ├── WebMvcAutoConfiguration → Configure DispatcherServlet
   └── SecurityAutoConfiguration → Create filter chain

6. EMBEDDED TOMCAT START (5000-6000ms)
   ├── Initialize connectors
   ├── Bind to port 8080
   └── Start accepting requests

═══════════════════════════════════════════════════════════════════
Application Ready! Listening on port 8080
```

### 5.2 Controller Layer

```java
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    // Constructor Injection (Spring injects dependencies)
    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequest request,  // Deserialized from JSON
            @AuthenticationPrincipal String email            // Extracted from JWT
    ) {
        // 1. Get current user from security context
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Map DTO to Entity
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setAuthor(author);

        // 3. Delegate to service layer
        Post saved = postService.create(post);

        // 4. Map Entity to DTO and return
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityMapper.toDto(saved));
    }
}
```

### 5.3 Request Processing Pipeline

```
Incoming HTTP Request
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│                    TOMCAT CONNECTOR                           │
│  • Accept TCP connection                                      │
│  • Parse HTTP protocol                                        │
│  • Create HttpServletRequest/Response                         │
└───────────────────────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│                  FILTER CHAIN (in order)                      │
│                                                               │
│  1. CorsFilter                                                │
│     └── Check Origin header against allowed origins           │
│     └── Add CORS headers to response                          │
│                                                               │
│  2. JwtAuthenticationFilter                                   │
│     └── Extract "Bearer" token from Authorization header      │
│     └── Validate JWT signature and expiration                 │
│     └── Create Authentication object                          │
│     └── Set in SecurityContextHolder                          │
│                                                               │
│  3. AuthorizationFilter                                       │
│     └── Check if user has required roles                      │
└───────────────────────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│                   DISPATCHER SERVLET                          │
│                                                               │
│  1. HandlerMapping                                            │
│     └── Match URL pattern to @RequestMapping                  │
│     └── Find: POST /api/v1/posts → PostController.createPost  │
│                                                               │
│  2. HandlerAdapter                                            │
│     └── Resolve method arguments                              │
│         • @RequestBody → Jackson deserialize JSON             │
│         • @PathVariable → Extract from URL                    │
│         • @AuthenticationPrincipal → From SecurityContext     │
│                                                               │
│  3. Invoke Controller Method                                  │
│                                                               │
│  4. Handle Return Value                                       │
│     └── ResponseEntity → Set status and headers               │
│     └── @ResponseBody → Jackson serialize to JSON             │
└───────────────────────────────────────────────────────────────┘
```

---

## 6. Security Layer

### 6.1 Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors()                           // Enable CORS
            .and()
            .csrf(csrf -> csrf.disable())     // Disable CSRF (using JWT)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()   // Public
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated() // All other require auth
            )
            .addFilterBefore(                 // Add JWT filter
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
```

### 6.2 JWT Authentication Flow

```
LOGIN REQUEST
═══════════════════════════════════════════════════════════════════

1. User submits credentials
   POST /api/v1/auth/login
   { "username": "zakri", "password": "secret123" }

2. AuthController.login()
   ├── Find user by username/email
   ├── AuthenticationManager.authenticate()
   │   └── BCrypt.matches(password, hashedPassword)
   └── Generate JWT token

3. JWT Token Structure:
   ┌─────────────────────────────────────────────────────────────┐
   │ HEADER (Base64)                                             │
   │ {"alg":"HS512"}                                             │
   ├─────────────────────────────────────────────────────────────┤
   │ PAYLOAD (Base64)                                            │
   │ {                                                           │
   │   "sub": "zakri@email.com",                                 │
   │   "role": "USER",                                           │
   │   "iat": 1736678400,                                        │
   │   "exp": 1736764800                                         │
   │ }                                                           │
   ├─────────────────────────────────────────────────────────────┤
   │ SIGNATURE                                                   │
   │ HMACSHA512(base64(header) + "." + base64(payload), secret)  │
   └─────────────────────────────────────────────────────────────┘

4. Token returned to client
   { "token": "eyJhbGciOiJIUzUxMiJ9...", "user": {...} }


AUTHENTICATED REQUEST
═══════════════════════════════════════════════════════════════════

1. Client sends request with token
   GET /api/v1/feed
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

2. JwtAuthenticationFilter.doFilter()
   ├── Extract token from header
   ├── Validate signature (using secret key)
   ├── Check expiration
   ├── Parse claims (email, role)
   └── Create Authentication object

3. Set SecurityContext
   SecurityContextHolder.getContext().setAuthentication(auth)

4. Controller can access user:
   @AuthenticationPrincipal String email
```

### 6.3 Password Hashing (BCrypt)

```
Password Storage:
═══════════════════════════════════════════════════════════════════

Original Password:  "secret123"
                         │
                         ▼
              ┌─────────────────────┐
              │   BCrypt Hash       │
              │   Cost factor: 10   │
              │   Random salt       │
              └─────────────────────┘
                         │
                         ▼
Stored Hash:  "$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0Jv5X6qVxZQbJqK8uxZ1p3kqDG"
               │  │   │
               │  │   └── Random salt (22 chars)
               │  └────── Cost factor (10 = 2^10 iterations)
               └──────── Algorithm identifier ($2a$ = BCrypt)

Verification:
1. Extract salt from stored hash
2. Hash input password with same salt
3. Compare resulting hash with stored hash
```

---

## 7. Service Layer

### 7.1 Business Logic Encapsulation

```java
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final SubscriptionRepository subscriptionRepository;
    private final HtmlSanitizer htmlSanitizer;

    // Constructor injection ensures all dependencies are provided
    public PostService(PostRepository postRepository, 
                       NotificationService notificationService,
                       SubscriptionRepository subscriptionRepository,
                       HtmlSanitizer htmlSanitizer) {
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.htmlSanitizer = htmlSanitizer;
    }

    public Post create(Post post) {
        // Step 1: Validate business rules
        if (post.getAuthor().isBanned()) {
            throw new BusinessException("Banned users cannot create posts");
        }

        // Step 2: Sanitize input (XSS prevention)
        post.setTitle(htmlSanitizer.sanitizePlainText(post.getTitle()));
        post.setDescription(htmlSanitizer.sanitizeRichText(post.getDescription()));

        // Step 3: Persist to database
        Post saved = postRepository.save(post);

        // Step 4: Side effects (notifications)
        notifySubscribers(saved);

        return saved;
    }

    private void notifySubscribers(Post post) {
        // Get all users subscribed to the author
        List<Subscription> subs = subscriptionRepository
            .findByUserId(post.getAuthor().getId());

        for (Subscription sub : subs) {
            if (!sub.getSubscriberId().equals(post.getAuthor().getId())) {
                notificationService.createNotification(
                    sub.getSubscriberId(),
                    "new_post",
                    "@" + post.getAuthor().getUsername() + " posted: " + post.getTitle()
                );
            }
        }
    }
}
```

### 7.2 Transaction Management

```
@Transactional Behavior:
═══════════════════════════════════════════════════════════════════

Method Entry
     │
     ▼
┌────────────────────────────┐
│ BEGIN TRANSACTION          │
│ Get connection from pool   │
│ Set autocommit = false     │
└────────────────────────────┘
     │
     ▼
┌────────────────────────────┐
│ EXECUTE BUSINESS LOGIC     │
│ • Repository.save(post)    │
│ • Repository.save(notif)   │
│ All changes in same TX     │
└────────────────────────────┘
     │
     ├─── SUCCESS ───────────┐
     │                       │
     ▼                       ▼
┌────────────┐         ┌────────────┐
│ COMMIT     │         │ ROLLBACK   │
│ All or     │         │ Undo all   │
│ nothing    │         │ changes    │
└────────────┘         └────────────┘
                             │
                       EXCEPTION ←───┘
```

---

## 8. Data Access Layer

### 8.1 Repository Pattern

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Spring Data JPA generates implementation automatically!
    
    // Derived query methods (parsed from method name):
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    Optional<Post> findByIdAndHiddenFalse(Long id);
    
    // Custom JPQL query:
    @Query("SELECT p FROM Post p WHERE p.hidden = false ORDER BY p.createdAt DESC")
    Page<Post> findAllVisible(Pageable pageable);
    
    // Native SQL query:
    @Query(value = "SELECT * FROM posts WHERE author_id IN :authorIds", 
           nativeQuery = true)
    List<Post> findByAuthorIds(@Param("authorIds") List<Long> authorIds);
}
```

### 8.2 JPA Entity Lifecycle

```
Entity Lifecycle States:
═══════════════════════════════════════════════════════════════════

                          ┌──────────────────┐
                          │     TRANSIENT    │
                          │   (new object)   │
                          └────────┬─────────┘
                                   │ persist()
                                   ▼
    ┌─────────────┐         ┌──────────────────┐
    │   DETACHED  │◄────────│     MANAGED      │
    │  (outside   │ detach/ │  (in persistence │
    │   context)  │ close   │     context)     │
    └──────┬──────┘         └────────┬─────────┘
           │                         │
           │ merge()                 │ remove()
           │                         ▼
           └────────────────►┌──────────────────┐
                             │     REMOVED      │
                             │  (marked for     │
                             │    deletion)     │
                             └──────────────────┘

Example:
═══════════════════════════════════════════════════════════════════

Post post = new Post();           // TRANSIENT (not tracked)
post.setTitle("Hello");

postRepository.save(post);        // MANAGED (tracked, has ID)

post.setTitle("Updated");         // Still MANAGED - change tracked!
// No need to call save() again - Hibernate dirty checking

// Transaction ends → DETACHED (no longer tracked)
```

### 8.3 Hibernate SQL Generation

```
JPQL → SQL Translation:
═══════════════════════════════════════════════════════════════════

Java Repository Method:
    postRepository.findByAuthorIdOrderByCreatedAtDesc(1L)

JPQL Generated:
    SELECT p FROM Post p WHERE p.author.id = :authorId 
    ORDER BY p.createdAt DESC

SQL Executed:
    SELECT 
        p.id, p.title, p.description, p.media_urls,
        p.created_at, p.updated_at, p.hidden, p.author_id
    FROM posts p
    WHERE p.author_id = 1
    ORDER BY p.created_at DESC

Execution Plan:
    1. Check connection pool for available connection
    2. Prepare SQL statement
    3. Bind parameters (author_id = 1)
    4. Execute query
    5. Map ResultSet to Post entities
    6. Add entities to Persistence Context (cache)
    7. Return List<Post>
```

### 8.4 N+1 Query Problem

```
The Problem:
═══════════════════════════════════════════════════════════════════

// Fetching 100 posts with their authors (LAZY loading)
List<Post> posts = postRepository.findAll();  // 1 query

for (Post post : posts) {
    System.out.println(post.getAuthor().getUsername());  // 100 queries!
}

// Total: 101 queries! (1 + N)

The Solution (JOIN FETCH):
═══════════════════════════════════════════════════════════════════

@Query("SELECT p FROM Post p JOIN FETCH p.author")
List<Post> findAllWithAuthors();  // 1 query with JOIN

SQL Generated:
    SELECT p.*, u.*
    FROM posts p
    INNER JOIN users u ON p.author_id = u.id

// Total: 1 query!
```

---

## 9. Database Layer

### 9.1 PostgreSQL Query Execution

```
Query Processing Pipeline:
═══════════════════════════════════════════════════════════════════

1. PARSER
   SQL String → Parse Tree
   "SELECT * FROM posts WHERE id = 1"
         │
         ▼
   ┌─────────────────────────┐
   │     SELECT              │
   │       │                 │
   │     FROM: posts         │
   │       │                 │
   │     WHERE: id = 1       │
   └─────────────────────────┘

2. ANALYZER
   Validate table/column names
   Resolve data types
         │
         ▼

3. REWRITER
   Apply rules
   Expand views
         │
         ▼

4. PLANNER/OPTIMIZER
   Choose execution strategy
   ┌─────────────────────────────────────────────┐
   │ Options:                                    │
   │   A. Sequential Scan: O(n) - scan all rows  │
   │   B. Index Scan: O(log n) - use B-tree      │
   │                                             │
   │ Cost Analysis:                              │
   │   Table size: 10,000 rows                   │
   │   Index on 'id': YES                        │
   │                                             │
   │ Decision: Use Index Scan on posts_pkey      │
   └─────────────────────────────────────────────┘
         │
         ▼

5. EXECUTOR
   Execute plan
   Return results
```

### 9.2 Database Schema

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    role VARCHAR(50) DEFAULT 'USER',
    bio TEXT,
    avatar_url VARCHAR(500),
    banned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts table
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users(id),
    title VARCHAR(255),
    description TEXT,  -- HTML content (sanitized)
    media_urls TEXT,   -- JSON array of URLs
    hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Comments table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    parent_id BIGINT REFERENCES comments(id),  -- For nested comments
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_created ON posts(created_at DESC);
CREATE INDEX idx_comments_post ON comments(post_id);
```

### 9.3 Connection Pooling (HikariCP)

```
Connection Pool Architecture:
═══════════════════════════════════════════════════════════════════

Application Threads                    Connection Pool
                                       
Thread 1 ──────┐                    ┌─────────────────┐
               │                    │  Pool Size: 10  │
Thread 2 ──────┼───► Request ──────►├─────────────────┤
               │    Connection      │ ○ ○ ○ ○ ○       │ 5 idle
Thread 3 ──────┤                    │ ● ● ● ● ●       │ 5 in use
               │                    └─────────────────┘
Thread 4 ──────┘                           │
                                           │
                    ◄──────────────────────┘
                    Return when done


Benefits:
• Avoid connection creation overhead (100-500ms saved per request)
• Limit database connections (prevent overload)
• Connection reuse
• Health checking

Configuration (application.yaml):
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

---

## 10. Memory Management

### 10.1 JVM Memory Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                         JVM HEAP                                │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    YOUNG GENERATION                       │  │
│  │  ┌────────────────┐  ┌─────────┐  ┌─────────┐            │  │
│  │  │     EDEN       │  │   S0    │  │   S1    │            │  │
│  │  │  New objects   │  │Survivor │  │Survivor │            │  │
│  │  │   created      │  │  space  │  │  space  │            │  │
│  │  └────────────────┘  └─────────┘  └─────────┘            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                     Minor GC │ (frequent, fast)                │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    OLD GENERATION                         │  │
│  │                                                           │  │
│  │    Long-lived objects (Spring beans, caches, etc.)        │  │
│  │                                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                     Major GC │ (rare, slow)
                              ▼

┌─────────────────────────────────────────────────────────────────┐
│                        METASPACE                                │
│         Class definitions, method metadata                      │
│    (Loaded once at startup, rarely collected)                   │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Object Lifecycle in a Request

```
Request Processing Memory:
═══════════════════════════════════════════════════════════════════

1. REQUEST ARRIVES
   ┌─────────────────────────────────────────────────┐
   │ STACK (Thread-specific)                         │
   │ ├── HttpServletRequest reference                │
   │ ├── Local variables                             │
   │ └── Method call frames                          │
   └─────────────────────────────────────────────────┘
   ┌─────────────────────────────────────────────────┐
   │ HEAP (Shared)                                   │
   │ ├── HttpServletRequest object ──► Eden space   │
   │ ├── DTO objects (CreatePostRequest)            │
   │ └── Entity objects (Post, User)                │
   └─────────────────────────────────────────────────┘

2. DURING PROCESSING
   • DTOs created → Eden space
   • Entities loaded from DB → Eden space
   • Spring beans → Old generation (long-lived)
   
3. RESPONSE SENT
   • Request thread returns to pool
   • Stack frame popped
   • Objects in Eden become garbage

4. GARBAGE COLLECTION
   • Minor GC runs (every few seconds)
   • Eden space cleared
   • Request objects collected
   • Memory reclaimed
```

### 10.3 Spring Bean Scopes & Memory

```
Bean Scopes:
═══════════════════════════════════════════════════════════════════

SINGLETON (Default)
┌────────────────────────────────────────────────────────────────┐
│  Scope: Application lifetime                                   │
│  Memory: Old generation (long-lived)                           │
│  Instance: ONE per ApplicationContext                          │
│                                                                │
│  @Service                                                       │
│  public class PostService { }  // Same instance for all        │
│                                                                │
│  Thread Safety: MUST be thread-safe (no mutable state)         │
└────────────────────────────────────────────────────────────────┘

PROTOTYPE
┌────────────────────────────────────────────────────────────────┐
│  Scope: Per injection                                          │
│  Memory: Eden → GC when done                                   │
│  Instance: NEW instance every time                             │
│                                                                │
│  @Scope("prototype")                                            │
│  @Component                                                     │
│  public class RequestProcessor { }                              │
└────────────────────────────────────────────────────────────────┘

REQUEST (Web only)
┌────────────────────────────────────────────────────────────────┐
│  Scope: Single HTTP request                                    │
│  Memory: Eden → GC after response                              │
│  Instance: NEW instance per request                            │
│                                                                │
│  @Scope(value = WebApplicationContext.SCOPE_REQUEST,            │
│         proxyMode = ScopedProxyMode.TARGET_CLASS)               │
│  @Component                                                     │
│  public class RequestContext { }                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 11. Complete Example: Creating a Post

### End-to-End Flow

```
TIME    LOCATION              ACTION
═══════════════════════════════════════════════════════════════════

0ms     BROWSER (UI)
        └── User clicks "Post" button
        └── Angular event handler triggered

1ms     BROWSER (Angular)
        └── PostEditorComponent.submitPost()
        └── Collect form data: { title, description, mediaUrls }
        └── Call PostService.createPost(data)

2ms     BROWSER (Angular Service)
        └── PostService.createPost()
        └── return this.http.post('/api/v1/posts', data)
        └── Observable created (lazy - not sent yet)

3ms     BROWSER (Angular Component)
        └── .subscribe() called
        └── Observable executes
        └── HTTP request initiated

4ms     BROWSER (HTTP Interceptor)
        └── AuthInterceptor.intercept()
        └── Get token: localStorage.getItem('accessToken')
        └── Clone request, add header:
            Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

5ms     BROWSER → NETWORK
        └── XMLHttpRequest sent
        └── TCP connection to localhost:8080
        └── HTTP POST request transmitted

───────────────────── NETWORK TRANSIT ──────────────────────────

50ms    SERVER (Tomcat Connector)
        └── TCP connection accepted
        └── HTTP request parsed
        └── Create HttpServletRequest object
        └── Allocate thread from pool (Heap: ~1KB)

51ms    SERVER (CorsFilter)
        └── Check Origin header
        └── Origin: http://localhost:4200 ✓
        └── Add CORS headers to response

52ms    SERVER (JwtAuthenticationFilter)
        └── Extract Authorization header
        └── Parse "Bearer " prefix
        └── Token: eyJhbGciOiJIUzUxMiJ9...
        └── Validate signature with HMAC-SHA512
        └── Check expiration: exp > now ✓
        └── Parse claims: { sub: "user@email.com", role: "USER" }
        └── Create Authentication object
        └── SecurityContextHolder.setAuthentication(auth)

53ms    SERVER (DispatcherServlet)
        └── HandlerMapping: Match URL to controller
        └── POST /api/v1/posts → PostController.createPost()

54ms    SERVER (Argument Resolution)
        └── @RequestBody CreatePostRequest
        │   └── Jackson ObjectMapper.readValue()
        │   └── JSON → CreatePostRequest (Heap: ~200 bytes)
        │
        └── @AuthenticationPrincipal String email
            └── SecurityContextHolder.getContext()
            └── Extract principal: "user@email.com"

55ms    SERVER (PostController)
        └── createPost(request, email) invoked
        └── Stack frame created (~500 bytes)

56ms    SERVER (PostController → UserRepository)
        └── userRepository.findByEmail("user@email.com")
        └── JPA generates JPQL → SQL
        └── SELECT * FROM users WHERE email = ?
        └── Execute via HikariCP connection
        └── ResultSet → User entity (Heap: ~300 bytes)
        └── Cache in Persistence Context

57ms    SERVER (PostController)
        └── Create Post entity
        └── post.setTitle(request.getTitle())
        └── post.setAuthor(user)
        └── Call postService.create(post)

58ms    SERVER (PostService)
        └── @Transactional begins
        └── HikariCP: Get connection from pool
        └── Set autocommit = false

59ms    SERVER (PostService → HtmlSanitizer)
        └── Sanitize title (remove HTML tags)
        └── Sanitize description (allow safe HTML)

60ms    SERVER (PostService → PostRepository)
        └── postRepository.save(post)
        └── Hibernate: Detect TRANSIENT entity
        └── Generate: INSERT INTO posts (...) VALUES (...)
        └── Execute INSERT
        └── Return generated ID
        └── Post entity now MANAGED (ID assigned)

65ms    SERVER (PostService → NotificationService)
        └── Find subscribers to author
        └── For each subscriber:
        │   └── Create Notification entity
        │   └── notificationRepository.save(notification)
        │   └── INSERT INTO notifications ...
        └── All in same transaction

70ms    SERVER (PostService)
        └── Method returns
        └── @Transactional commits
        └── Connection.commit()
        └── Return connection to pool

71ms    SERVER (PostController)
        └── EntityMapper.toDto(savedPost)
        └── Post → PostDto (Heap: ~400 bytes)
        └── Return ResponseEntity.created(dto)

72ms    SERVER (ResponseBodyAdvice)
        └── Jackson ObjectMapper.writeValueAsString()
        └── PostDto → JSON string

73ms    SERVER (DispatcherServlet)
        └── Write JSON to HttpServletResponse
        └── Set Content-Type: application/json
        └── Set status: 201 Created

74ms    SERVER (Tomcat)
        └── Flush response buffer
        └── Send HTTP response

───────────────────── NETWORK TRANSIT ──────────────────────────

120ms   BROWSER (Network)
        └── HTTP response received
        └── Status: 201 Created
        └── Body: { "id": 42, "title": "...", ... }

121ms   BROWSER (HttpClient)
        └── Parse JSON response
        └── Create Post object

122ms   BROWSER (Angular Component)
        └── .subscribe(post => {...}) callback
        └── this.posts.unshift(post)  // Add to top of list
        └── Change detection triggered
        └── View updated

123ms   BROWSER (UI)
        └── New post visible in feed
        └── Toast notification: "Post created!"

═══════════════════════════════════════════════════════════════════
TOTAL TIME: ~123ms
MEMORY ALLOCATED: ~5KB (mostly garbage collected after response)
DATABASE OPERATIONS: 3 (SELECT user, INSERT post, INSERT notification)
```

---

## 📚 Summary

This document covered:

1. **Architecture** - How all layers connect and communicate
2. **Request Flow** - Step-by-step journey from click to database
3. **Angular** - Components, services, interceptors, guards
4. **Spring Boot** - Controllers, services, repositories
5. **Security** - JWT authentication, BCrypt hashing, filter chain
6. **Hibernate/JPA** - Entity lifecycle, persistence context, SQL generation
7. **PostgreSQL** - Query execution, connection pooling
8. **Memory** - JVM heap, garbage collection, bean scopes

Understanding these layers helps you:
- Debug issues faster
- Optimize performance
- Make better architectural decisions
- Write more efficient code
