# Collections Framework

The Java Collections Framework provides a unified architecture for representing and manipulating collections.

## Collection Hierarchy

```
                    Iterable
                       │
                   Collection
                       │
       ┌───────────────┼───────────────┐
       │               │               │
      List            Set            Queue
       │               │               │
   ArrayList       HashSet      PriorityQueue
   LinkedList      TreeSet        ArrayDeque
   Vector       LinkedHashSet
                       
                      Map
                       │
       ┌───────────────┼───────────────┐
       │               │               │
   HashMap         TreeMap      LinkedHashMap
   Hashtable   ConcurrentHashMap
```

---

## List Interface

Ordered collection that allows duplicates.

### ArrayList
```java
// Dynamic array - fast random access O(1)
List<Post> posts = new ArrayList<>();
posts.add(new Post("First"));
posts.add(new Post("Second"));
Post first = posts.get(0);  // O(1)
```

### LinkedList
```java
// Doubly-linked list - fast insertion/deletion O(1)
List<Post> posts = new LinkedList<>();
posts.addFirst(new Post("First"));
posts.addLast(new Post("Last"));
```

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| get(index) | O(1) | O(n) |
| add(element) | O(1)* | O(1) |
| add(index, element) | O(n) | O(n) |
| remove(index) | O(n) | O(n) |

---

## Set Interface

Collection that contains no duplicates.

### HashSet
```java
// Uses hashCode() - no order guaranteed
Set<String> tags = new HashSet<>();
tags.add("java");
tags.add("spring");
tags.add("java");  // Ignored - duplicate
System.out.println(tags.size());  // 2
```

### LinkedHashSet
```java
// Maintains insertion order
Set<String> tags = new LinkedHashSet<>();
```

### TreeSet
```java
// Sorted order (natural or custom Comparator)
Set<String> tags = new TreeSet<>();
tags.add("spring");
tags.add("java");
tags.add("angular");
// [angular, java, spring]
```

---

## Map Interface

Key-value pairs - keys are unique.

### HashMap
```java
Map<String, User> userCache = new HashMap<>();
userCache.put("john@email.com", john);
userCache.put("jane@email.com", jane);

User user = userCache.get("john@email.com");
boolean exists = userCache.containsKey("john@email.com");

// Iteration
for (Map.Entry<String, User> entry : userCache.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}
```

### Common Map Operations
```java
// putIfAbsent
userCache.putIfAbsent("john@email.com", newJohn);

// getOrDefault
User user = userCache.getOrDefault("unknown", defaultUser);

// computeIfAbsent
userCache.computeIfAbsent("new@email.com", email -> createUser(email));

// merge
Map<String, Integer> wordCount = new HashMap<>();
wordCount.merge("java", 1, Integer::sum);
```

---

## Queue Interface

FIFO (First-In-First-Out) collection.

```java
Queue<Task> taskQueue = new LinkedList<>();
taskQueue.offer(new Task("Task 1"));  // Add
taskQueue.offer(new Task("Task 2"));

Task next = taskQueue.poll();  // Remove and return
Task peek = taskQueue.peek();  // Return without removing
```

### Deque (Double-ended Queue)
```java
Deque<String> deque = new ArrayDeque<>();
deque.addFirst("first");
deque.addLast("last");
String first = deque.removeFirst();
String last = deque.removeLast();
```

---

## Utility Methods

### Collections Class
```java
List<Post> posts = new ArrayList<>();

// Sorting
Collections.sort(posts);
Collections.sort(posts, Comparator.comparing(Post::getCreatedAt));

// Searching
int index = Collections.binarySearch(posts, target);

// Shuffling
Collections.shuffle(posts);

// Immutable views
List<Post> readOnly = Collections.unmodifiableList(posts);

// Synchronized wrappers
List<Post> syncList = Collections.synchronizedList(posts);
```

### Arrays Class
```java
String[] arr = {"java", "spring", "angular"};
List<String> list = Arrays.asList(arr);  // Fixed-size list

// Java 9+
List<String> immutable = List.of("java", "spring");
Set<String> set = Set.of("java", "spring");
Map<String, Integer> map = Map.of("java", 1, "spring", 2);
```

---

## Choosing the Right Collection

| Scenario | Collection |
|----------|------------|
| Ordered, allow duplicates | ArrayList |
| Frequent insertions/deletions | LinkedList |
| No duplicates, no order | HashSet |
| No duplicates, sorted | TreeSet |
| Key-value pairs | HashMap |
| Sorted key-value pairs | TreeMap |
| FIFO processing | LinkedList or ArrayDeque |
| Thread-safe map | ConcurrentHashMap |

---

## Real-World Example

```java
@Service
public class PostService {
    
    public Map<String, List<Post>> groupByAuthor(List<Post> posts) {
        return posts.stream()
            .collect(Collectors.groupingBy(Post::getAuthorName));
    }
    
    public Set<String> extractTags(List<Post> posts) {
        return posts.stream()
            .flatMap(post -> post.getTags().stream())
            .collect(Collectors.toSet());
    }
    
    public List<Post> sortByDate(List<Post> posts) {
        return posts.stream()
            .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }
}
```
