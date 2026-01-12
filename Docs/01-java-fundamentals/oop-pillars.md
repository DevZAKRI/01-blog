# OOP Pillars

## Encapsulation

Encapsulation is the bundling of data (attributes) and methods that operate on that data within a single unit (class), while restricting direct access to some components.

### Key Concepts
- **Private fields** with public getters/setters
- **Data hiding** - internal state is protected
- **Controlled access** through methods

```java
public class User {
    private String email;
    private String password;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email;
        }
    }
}
```

---

## Inheritance

Inheritance allows a class to inherit properties and methods from another class, promoting code reuse.

### Key Concepts
- **extends** keyword for class inheritance
- **super** keyword to access parent class
- **Method overriding**
- Java supports single inheritance only

```java
public class Post {
    protected String title;
    protected String content;
}

public class BlogPost extends Post {
    private List<Comment> comments;
    
    @Override
    public String toString() {
        return "BlogPost: " + title;
    }
}
```

---

## Polymorphism

Polymorphism allows objects to be treated as instances of their parent class, enabling one interface to be used for different underlying forms.

### Types
1. **Compile-time (Static)** - Method overloading
2. **Runtime (Dynamic)** - Method overriding

```java
// Method Overloading
public class PostService {
    public Post findById(Long id) { }
    public Post findById(String slug) { }
}

// Method Overriding
public interface Publishable {
    void publish();
}

public class BlogPost implements Publishable {
    @Override
    public void publish() {
        // Implementation
    }
}
```

---

## Abstraction

Abstraction hides complex implementation details and shows only the necessary features of an object.

### Achieved Through
- **Abstract classes**
- **Interfaces**

```java
public abstract class Content {
    public abstract void render();
    
    public void share() {
        // Common implementation
    }
}

public interface Searchable {
    List<Result> search(String query);
}
```

---

## Summary Table

| Pillar | Purpose | Java Implementation |
|--------|---------|---------------------|
| Encapsulation | Data hiding | Private fields, getters/setters |
| Inheritance | Code reuse | extends keyword |
| Polymorphism | Flexibility | Overloading/Overriding |
| Abstraction | Hide complexity | Abstract classes, Interfaces |
