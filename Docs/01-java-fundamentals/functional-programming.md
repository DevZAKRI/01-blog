# Functional Programming in Java

## Lambda Expressions

Lambda expressions provide a concise way to represent anonymous functions.

### Syntax

```java
// Full syntax
(parameters) -> { statements; }

// Simplified
(parameters) -> expression

// Examples
(a, b) -> a + b
x -> x * x
() -> System.out.println("Hello")
(String s) -> s.length()
```

### Functional Interfaces

A functional interface has exactly one abstract method.

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

### Common Functional Interfaces

| Interface | Method | Description |
|-----------|--------|-------------|
| `Function<T,R>` | `R apply(T t)` | Transform T to R |
| `Predicate<T>` | `boolean test(T t)` | Test condition |
| `Consumer<T>` | `void accept(T t)` | Consume value |
| `Supplier<T>` | `T get()` | Supply value |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | Two args → result |

```java
Function<Post, String> getTitle = Post::getTitle;
Predicate<Post> isPublished = Post::isPublished;
Consumer<Post> printPost = post -> System.out.println(post);
Supplier<Post> newPost = Post::new;
```

### Method References

```java
// Static method
Function<String, Integer> parse = Integer::parseInt;

// Instance method of particular object
Consumer<String> printer = System.out::println;

// Instance method of arbitrary object
Function<Post, String> getTitle = Post::getTitle;

// Constructor
Supplier<Post> creator = Post::new;
Function<String, Post> postFromTitle = Post::new;  // Constructor with String param
```

---

## Streams API

Streams provide a functional approach to processing collections.

### Stream Pipeline

```
Source → Intermediate Operations → Terminal Operation
   │              │                        │
Collection     filter()                 collect()
   Array        map()                   forEach()
   File        sorted()                  reduce()
   ...         distinct()                count()
               limit()                   ...
```

### Creating Streams

```java
// From collection
List<Post> posts = ...;
Stream<Post> stream = posts.stream();

// From array
String[] tags = {"java", "spring"};
Stream<String> tagStream = Arrays.stream(tags);

// Using Stream.of()
Stream<String> stream = Stream.of("a", "b", "c");

// Infinite streams
Stream<Integer> infinite = Stream.iterate(0, n -> n + 1);
Stream<Double> randoms = Stream.generate(Math::random);

// Range
IntStream range = IntStream.range(1, 10);  // 1-9
IntStream rangeClosed = IntStream.rangeClosed(1, 10);  // 1-10
```

### Intermediate Operations

```java
List<Post> posts = ...;

// filter - keep elements matching predicate
posts.stream()
    .filter(post -> post.isPublished())
    .filter(post -> post.getLikes() > 100);

// map - transform elements
posts.stream()
    .map(Post::getTitle)
    .map(String::toUpperCase);

// flatMap - flatten nested structures
posts.stream()
    .flatMap(post -> post.getTags().stream());

// distinct - remove duplicates
posts.stream()
    .map(Post::getAuthorId)
    .distinct();

// sorted
posts.stream()
    .sorted(Comparator.comparing(Post::getCreatedAt).reversed());

// limit & skip
posts.stream()
    .skip(10)
    .limit(5);

// peek - debug/logging
posts.stream()
    .peek(post -> log.debug("Processing: {}", post.getId()))
    .map(Post::getTitle);
```

### Terminal Operations

```java
// collect - gather results
List<String> titles = posts.stream()
    .map(Post::getTitle)
    .collect(Collectors.toList());

// forEach - perform action
posts.stream()
    .forEach(post -> process(post));

// count
long count = posts.stream()
    .filter(Post::isPublished)
    .count();

// reduce - combine elements
int totalLikes = posts.stream()
    .mapToInt(Post::getLikes)
    .reduce(0, Integer::sum);

// findFirst / findAny
Optional<Post> first = posts.stream()
    .filter(p -> p.getTitle().contains("Java"))
    .findFirst();

// anyMatch / allMatch / noneMatch
boolean hasPublished = posts.stream().anyMatch(Post::isPublished);
boolean allPublished = posts.stream().allMatch(Post::isPublished);
boolean noneDeleted = posts.stream().noneMatch(Post::isDeleted);

// min / max
Optional<Post> mostLiked = posts.stream()
    .max(Comparator.comparing(Post::getLikes));
```

### Collectors

```java
// toList, toSet
List<Post> list = posts.stream().collect(Collectors.toList());
Set<String> tags = posts.stream()
    .flatMap(p -> p.getTags().stream())
    .collect(Collectors.toSet());

// toMap
Map<Long, Post> postMap = posts.stream()
    .collect(Collectors.toMap(Post::getId, Function.identity()));

// joining
String allTitles = posts.stream()
    .map(Post::getTitle)
    .collect(Collectors.joining(", "));

// groupingBy
Map<Long, List<Post>> byAuthor = posts.stream()
    .collect(Collectors.groupingBy(Post::getAuthorId));

// partitioningBy
Map<Boolean, List<Post>> partitioned = posts.stream()
    .collect(Collectors.partitioningBy(Post::isPublished));

// counting, summing, averaging
Map<Long, Long> countByAuthor = posts.stream()
    .collect(Collectors.groupingBy(Post::getAuthorId, Collectors.counting()));

// summarizing
IntSummaryStatistics stats = posts.stream()
    .collect(Collectors.summarizingInt(Post::getLikes));
// stats.getSum(), stats.getAverage(), stats.getMax(), etc.
```

---

## Optional

Optional is a container that may or may not contain a value, helping avoid NullPointerException.

### Creating Optional

```java
// Of - value must not be null
Optional<Post> opt = Optional.of(post);

// OfNullable - value can be null
Optional<Post> opt = Optional.ofNullable(maybeNull);

// Empty
Optional<Post> empty = Optional.empty();
```

### Using Optional

```java
Optional<Post> postOpt = postRepository.findById(id);

// isPresent / isEmpty (Java 11+)
if (postOpt.isPresent()) {
    Post post = postOpt.get();
}

// ifPresent
postOpt.ifPresent(post -> process(post));

// ifPresentOrElse (Java 9+)
postOpt.ifPresentOrElse(
    post -> process(post),
    () -> log.warn("Post not found")
);

// orElse - provide default
Post post = postOpt.orElse(defaultPost);

// orElseGet - lazy default
Post post = postOpt.orElseGet(() -> createDefaultPost());

// orElseThrow
Post post = postOpt.orElseThrow(() -> new PostNotFoundException(id));

// or (Java 9+) - provide alternative Optional
Optional<Post> result = postOpt.or(() -> findFromCache(id));
```

### Transforming Optional

```java
// map - transform if present
Optional<String> title = postOpt.map(Post::getTitle);

// flatMap - when transformation returns Optional
Optional<User> author = postOpt.flatMap(post -> userService.findById(post.getAuthorId()));

// filter - conditional presence
Optional<Post> published = postOpt.filter(Post::isPublished);
```

### Optional Best Practices

```java
// ✅ Good - as return type
public Optional<Post> findBySlug(String slug) {
    return postRepository.findBySlug(slug);
}

// ❌ Bad - as field
public class Post {
    private Optional<String> subtitle;  // Don't do this
}

// ❌ Bad - as parameter
public void process(Optional<Post> post) { }  // Don't do this

// ✅ Good - chaining
String authorName = postOpt
    .flatMap(post -> userService.findById(post.getAuthorId()))
    .map(User::getName)
    .orElse("Unknown");
```

---

## Real-World Example

```java
@Service
public class PostService {
    
    public PostSummary getPostSummary(Long authorId) {
        List<Post> posts = postRepository.findByAuthorId(authorId);
        
        Map<String, Long> tagCounts = posts.stream()
            .filter(Post::isPublished)
            .flatMap(post -> post.getTags().stream())
            .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()
            ));
        
        IntSummaryStatistics likeStats = posts.stream()
            .filter(Post::isPublished)
            .collect(Collectors.summarizingInt(Post::getLikes));
        
        List<String> topPosts = posts.stream()
            .filter(Post::isPublished)
            .sorted(Comparator.comparing(Post::getLikes).reversed())
            .limit(5)
            .map(Post::getTitle)
            .collect(Collectors.toList());
        
        return new PostSummary(
            tagCounts,
            likeStats.getCount(),
            likeStats.getAverage(),
            topPosts
        );
    }
}
```
