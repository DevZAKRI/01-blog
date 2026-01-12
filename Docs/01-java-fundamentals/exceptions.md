# Exceptions (Checked/Unchecked)

## Exception Hierarchy

```
          Throwable
              │
      ┌───────┴───────┐
      │               │
    Error         Exception
      │               │
OutOfMemoryError     │
StackOverflowError   ├── RuntimeException (Unchecked)
                     │   ├── NullPointerException
                     │   ├── IllegalArgumentException
                     │   ├── IndexOutOfBoundsException
                     │   └── ...
                     │
                     └── Checked Exceptions
                         ├── IOException
                         ├── SQLException
                         └── ...
```

---

## Checked vs Unchecked Exceptions

### Checked Exceptions
- **Must be handled** at compile time
- Extend `Exception` (not `RuntimeException`)
- Represent recoverable conditions

```java
public void readFile(String path) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(path));
    // ...
}

// Must handle
try {
    readFile("posts.txt");
} catch (IOException e) {
    log.error("Failed to read file", e);
}
```

### Unchecked Exceptions
- **Do not require** explicit handling
- Extend `RuntimeException`
- Represent programming errors

```java
public Post getPostById(Long id) {
    if (id == null) {
        throw new IllegalArgumentException("ID cannot be null");
    }
    return postRepository.findById(id)
        .orElseThrow(() -> new PostNotFoundException("Post not found: " + id));
}
```

---

## Exception Handling

### try-catch-finally
```java
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("posts.txt"));
    String line = reader.readLine();
} catch (FileNotFoundException e) {
    log.error("File not found", e);
} catch (IOException e) {
    log.error("Error reading file", e);
} finally {
    if (reader != null) {
        try {
            reader.close();
        } catch (IOException e) {
            log.error("Error closing reader", e);
        }
    }
}
```

### try-with-resources (Java 7+)
```java
try (BufferedReader reader = new BufferedReader(new FileReader("posts.txt"))) {
    String line = reader.readLine();
} catch (IOException e) {
    log.error("Error reading file", e);
}
// reader is automatically closed
```

### Multi-catch (Java 7+)
```java
try {
    // code
} catch (IOException | SQLException e) {
    log.error("Database or IO error", e);
}
```

---

## Custom Exceptions

### Business Exception
```java
public class PostNotFoundException extends RuntimeException {
    
    public PostNotFoundException(String message) {
        super(message);
    }
    
    public PostNotFoundException(Long id) {
        super("Post not found with id: " + id);
    }
}
```

### Exception with Error Code
```java
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;
    
    public BusinessException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    // Getters
}
```

---

## Best Practices

### 1. Be Specific
```java
// Bad
catch (Exception e) { }

// Good
catch (PostNotFoundException e) {
    // Handle specific case
}
```

### 2. Don't Swallow Exceptions
```java
// Bad
try {
    // code
} catch (Exception e) {
    // Empty - exception is lost!
}

// Good
try {
    // code
} catch (Exception e) {
    log.error("Operation failed", e);
    throw new ServiceException("Operation failed", e);
}
```

### 3. Use Exception Chaining
```java
try {
    repository.save(post);
} catch (DataAccessException e) {
    throw new ServiceException("Failed to save post", e);  // Preserve original
}
```

### 4. Clean Up Resources
```java
// Always use try-with-resources for AutoCloseable resources
try (Connection conn = dataSource.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    // ...
}
```

---

## Spring Exception Handling

### @ControllerAdvice
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePostNotFound(PostNotFoundException ex) {
        return new ErrorResponse("POST_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.toList());
        return new ErrorResponse("VALIDATION_ERROR", errors);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
    }
}
```

### Error Response DTO
```java
public record ErrorResponse(
    String code,
    Object message,
    LocalDateTime timestamp
) {
    public ErrorResponse(String code, Object message) {
        this(code, message, LocalDateTime.now());
    }
}
```
