# Interfaces vs Abstract Classes

## Interfaces

An interface is a contract that defines what a class must do, without specifying how.

### Characteristics
- **100% abstraction** (before Java 8)
- **Multiple inheritance** supported
- **Implicitly public and abstract** methods
- **Constants only** (public static final)

### Modern Interfaces (Java 8+)

```java
public interface Publishable {
    // Abstract method
    void publish();
    
    // Default method (Java 8+)
    default void unpublish() {
        System.out.println("Unpublishing...");
    }
    
    // Static method (Java 8+)
    static boolean isPublishable(Object obj) {
        return obj instanceof Publishable;
    }
    
    // Private method (Java 9+)
    private void log(String message) {
        System.out.println(message);
    }
}
```

### Functional Interfaces

```java
@FunctionalInterface
public interface PostProcessor {
    Post process(Post post);
}

// Usage with lambda
PostProcessor processor = post -> {
    post.setUpdatedAt(LocalDateTime.now());
    return post;
};
```

---

## Abstract Classes

An abstract class is a class that cannot be instantiated and may contain both abstract and concrete methods.

### Characteristics
- **Partial abstraction**
- **Single inheritance** only
- **Can have constructors**
- **Can have instance variables**
- **Can have any access modifier**

```java
public abstract class Content {
    protected Long id;
    protected String title;
    protected LocalDateTime createdAt;
    
    // Constructor
    public Content(String title) {
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }
    
    // Abstract method
    public abstract void render();
    
    // Concrete method
    public String getFormattedDate() {
        return createdAt.format(DateTimeFormatter.ISO_DATE);
    }
}

public class BlogPost extends Content {
    private String body;
    
    public BlogPost(String title, String body) {
        super(title);
        this.body = body;
    }
    
    @Override
    public void render() {
        System.out.println(title + "\n" + body);
    }
}
```

---

## Comparison Table

| Feature | Interface | Abstract Class |
|---------|-----------|----------------|
| Multiple inheritance | ✓ | ✗ |
| Instance variables | ✗ (only constants) | ✓ |
| Constructors | ✗ | ✓ |
| Access modifiers | public only | Any |
| Default methods | ✓ (Java 8+) | ✓ |
| Static methods | ✓ (Java 8+) | ✓ |

---

## When to Use What

### Use Interface When:
- You need multiple inheritance
- You want to define a contract
- Unrelated classes need to implement common behavior
- You're designing an API

### Use Abstract Class When:
- You need to share code among related classes
- You need non-static, non-final fields
- You need access modifiers other than public
- You want to provide a common base with some implementation

---

## Real-World Example

```java
// Interface - defines capability
public interface Searchable {
    List<SearchResult> search(String query);
}

// Abstract class - defines common structure
public abstract class BaseEntity {
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

// Concrete class - combines both
public class Post extends BaseEntity implements Searchable {
    private String title;
    private String content;
    
    @Override
    public List<SearchResult> search(String query) {
        // Implementation
    }
}
```
