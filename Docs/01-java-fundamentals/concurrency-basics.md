# Java Concurrency Basics

## Thread Fundamentals

### Creating Threads

#### Extending Thread
```java
public class PostProcessor extends Thread {
    @Override
    public void run() {
        System.out.println("Processing in: " + Thread.currentThread().getName());
    }
}

// Usage
PostProcessor processor = new PostProcessor();
processor.start();  // NOT run() - start() creates new thread
```

#### Implementing Runnable
```java
public class PostTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Processing post...");
    }
}

// Usage
Thread thread = new Thread(new PostTask());
thread.start();

// Or with lambda
Thread thread = new Thread(() -> System.out.println("Processing..."));
thread.start();
```

---

## Thread Lifecycle

```
        ┌─────────┐
        │   NEW   │ ─────────────────────────────────────────┐
        └────┬────┘                                          │
             │ start()                                       │
             ▼                                               │
        ┌─────────┐                                          │
   ┌───→│ RUNNABLE│←───┐                                     │
   │    └────┬────┘    │                                     │
   │         │         │                                     │
   │         │ scheduled by OS                               │
   │         ▼         │                                     │
   │    ┌─────────┐    │ notify()/                           │
   │    │ RUNNING │────┤ notifyAll()/                        │
   │    └────┬────┘    │ timeout                             │
   │         │         │                                     │
   │         │ wait()/sleep()/                               │
   │         │ join()/blocked on I/O                         │
   │         ▼         │                                     │
   │    ┌─────────────┐│                                     │
   └────│BLOCKED/     ├┘                                     │
        │WAITING/     │                                      │
        │TIMED_WAITING│                                      │
        └─────────────┘                                      │
                                                             │
             run() completes or exception                    │
                           │                                 │
                           ▼                                 │
                    ┌────────────┐                           │
                    │ TERMINATED │ ←─────────────────────────┘
                    └────────────┘
```

---

## Synchronization

### synchronized Keyword

```java
public class Counter {
    private int count = 0;
    
    // Synchronized method
    public synchronized void increment() {
        count++;
    }
    
    // Synchronized block
    public void decrement() {
        synchronized (this) {
            count--;
        }
    }
    
    public synchronized int getCount() {
        return count;
    }
}
```

### Locks (java.util.concurrent.locks)

```java
public class PostCache {
    private final ReentrantLock lock = new ReentrantLock();
    private Map<Long, Post> cache = new HashMap<>();
    
    public Post get(Long id) {
        lock.lock();
        try {
            return cache.get(id);
        } finally {
            lock.unlock();
        }
    }
    
    public void put(Long id, Post post) {
        lock.lock();
        try {
            cache.put(id, post);
        } finally {
            lock.unlock();
        }
    }
}
```

### ReadWriteLock

```java
public class PostCache {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Map<Long, Post> cache = new HashMap<>();
    
    public Post get(Long id) {
        rwLock.readLock().lock();  // Multiple readers allowed
        try {
            return cache.get(id);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    public void put(Long id, Post post) {
        rwLock.writeLock().lock();  // Exclusive access
        try {
            cache.put(id, post);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

---

## Executors Framework

### ExecutorService

```java
// Fixed thread pool
ExecutorService executor = Executors.newFixedThreadPool(4);

// Execute tasks
executor.execute(() -> processPost(post1));
executor.execute(() -> processPost(post2));

// Shutdown
executor.shutdown();
try {
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
}
```

### Common Executor Types

```java
// Fixed number of threads
ExecutorService fixed = Executors.newFixedThreadPool(4);

// Cached (dynamic) thread pool
ExecutorService cached = Executors.newCachedThreadPool();

// Single thread
ExecutorService single = Executors.newSingleThreadExecutor();

// Scheduled
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.scheduleAtFixedRate(
    () -> cleanupCache(), 
    0, 1, TimeUnit.HOURS
);
```

---

## Future & Callable

### Callable (returns value)

```java
public class PostCounter implements Callable<Integer> {
    private final Long userId;
    
    public PostCounter(Long userId) {
        this.userId = userId;
    }
    
    @Override
    public Integer call() throws Exception {
        return postRepository.countByUserId(userId);
    }
}

// Usage
ExecutorService executor = Executors.newFixedThreadPool(4);
Future<Integer> future = executor.submit(new PostCounter(userId));

// Get result (blocks until complete)
try {
    Integer count = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
}
```

### Multiple Futures

```java
List<Callable<Post>> tasks = userIds.stream()
    .map(id -> (Callable<Post>) () -> fetchUserPosts(id))
    .collect(Collectors.toList());

List<Future<Post>> futures = executor.invokeAll(tasks);

for (Future<Post> future : futures) {
    Post post = future.get();
    // process
}
```

---

## CompletableFuture (Java 8+)

### Basic Usage

```java
CompletableFuture<Post> future = CompletableFuture.supplyAsync(
    () -> postService.findById(id)
);

future.thenAccept(post -> System.out.println(post.getTitle()));
```

### Chaining

```java
CompletableFuture.supplyAsync(() -> postService.findById(id))
    .thenApply(post -> enrichWithComments(post))
    .thenApply(post -> enrichWithAuthor(post))
    .thenAccept(post -> cache.put(post))
    .exceptionally(ex -> {
        log.error("Failed", ex);
        return null;
    });
```

### Combining Futures

```java
CompletableFuture<User> userFuture = CompletableFuture
    .supplyAsync(() -> userService.findById(userId));

CompletableFuture<List<Post>> postsFuture = CompletableFuture
    .supplyAsync(() -> postService.findByUserId(userId));

// Combine results
CompletableFuture<UserProfile> profileFuture = userFuture
    .thenCombine(postsFuture, (user, posts) -> new UserProfile(user, posts));

// Wait for all
CompletableFuture.allOf(future1, future2, future3).join();

// Wait for any
CompletableFuture.anyOf(future1, future2, future3).join();
```

---

## Thread-Safe Collections

```java
// ConcurrentHashMap
Map<String, Post> cache = new ConcurrentHashMap<>();

// CopyOnWriteArrayList (good for read-heavy)
List<Listener> listeners = new CopyOnWriteArrayList<>();

// BlockingQueue
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
queue.put(task);      // Blocks if full
Task t = queue.take(); // Blocks if empty

// Atomic variables
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();
counter.compareAndSet(expected, newValue);
```

---

## Spring Async Support

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {
    
    @Async
    public CompletableFuture<Void> sendNotification(Notification notification) {
        // This runs in separate thread
        emailService.send(notification);
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## Best Practices

1. **Prefer high-level concurrency utilities** over raw threads
2. **Use immutable objects** when possible
3. **Minimize synchronized blocks**
4. **Always shutdown ExecutorServices**
5. **Handle InterruptedException properly**
6. **Avoid nested locks** (deadlock risk)
7. **Use concurrent collections** instead of synchronized wrappers
