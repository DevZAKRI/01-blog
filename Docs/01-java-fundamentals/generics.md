# Generics

Generics enable types (classes and interfaces) to be parameters when defining classes, interfaces, and methods.

## Why Generics?

### Without Generics
```java
List list = new ArrayList();
list.add("post");
list.add(123); // No compile-time error
String s = (String) list.get(1); // Runtime ClassCastException!
```

### With Generics
```java
List<String> list = new ArrayList<>();
list.add("post");
// list.add(123); // Compile-time error!
String s = list.get(0); // No cast needed
```

---

## Generic Classes

```java
public class ApiResponse<T> {
    private T data;
    private String message;
    private boolean success;
    
    public ApiResponse(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
    }
    
    public T getData() {
        return data;
    }
}

// Usage
ApiResponse<Post> postResponse = new ApiResponse<>(post, "Success", true);
ApiResponse<List<User>> usersResponse = new ApiResponse<>(users, "Success", true);
```

---

## Generic Methods

```java
public class Utils {
    public static <T> T getFirstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }
    
    public static <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}

// Usage
Post first = Utils.getFirstOrNull(posts);
Map<String, Integer> map = Utils.createMap("count", 42);
```

---

## Bounded Type Parameters

### Upper Bound (extends)
```java
// T must be Number or its subclass
public class Statistics<T extends Number> {
    private List<T> numbers;
    
    public double average() {
        return numbers.stream()
            .mapToDouble(Number::doubleValue)
            .average()
            .orElse(0.0);
    }
}
```

### Multiple Bounds
```java
// T must extend BaseEntity AND implement Serializable
public class Repository<T extends BaseEntity & Serializable> {
    public void save(T entity) { }
}
```

---

## Wildcards

### Unbounded Wildcard (?)
```java
public void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
}
```

### Upper Bounded Wildcard (? extends)
```java
// Accepts List<Number>, List<Integer>, List<Double>, etc.
public double sum(List<? extends Number> numbers) {
    return numbers.stream()
        .mapToDouble(Number::doubleValue)
        .sum();
}
```

### Lower Bounded Wildcard (? super)
```java
// Accepts List<Integer>, List<Number>, List<Object>
public void addIntegers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
    list.add(3);
}
```

---

## PECS Principle

**Producer Extends, Consumer Super**

```java
// Producer - reads from collection (extends)
public void copy(List<? extends Post> source, List<? super Post> dest) {
    for (Post post : source) {  // source produces Posts
        dest.add(post);          // dest consumes Posts
    }
}
```

---

## Type Erasure

Generics are implemented through **type erasure** - type information is removed at runtime.

```java
// At compile time
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();

// At runtime (both become)
List strings = new ArrayList();
List integers = new ArrayList();

// This is why you can't do:
// if (obj instanceof List<String>) { } // Won't compile
```

---

## Common Conventions

| Letter | Meaning |
|--------|---------|
| T | Type |
| E | Element |
| K | Key |
| V | Value |
| N | Number |
| S, U, V | 2nd, 3rd, 4th types |

---

## Real-World Example

```java
public interface BaseRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void deleteById(ID id);
}

public interface PostRepository extends BaseRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
    Optional<Post> findBySlug(String slug);
}
```
