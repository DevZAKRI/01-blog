# Garbage Collection & Memory Layout

## JVM Memory Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                         JVM Memory                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    HEAP (Shared)                        │   │
│  │  ┌───────────────────┐  ┌─────────────────────────────┐ │   │
│  │  │   Young Gen       │  │      Old Gen (Tenured)      │ │   │
│  │  │ ┌─────┬─────────┐ │  │                             │ │   │
│  │  │ │Eden │Survivor │ │  │   Long-lived objects        │ │   │
│  │  │ │     │ S0 │ S1 │ │  │                             │ │   │
│  │  │ └─────┴────┴────┘ │  └─────────────────────────────┘ │   │
│  │  └───────────────────┘                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────┐  ┌────────────────────────────────────┐  │
│  │   Metaspace      │  │         Stack (per thread)        │  │
│  │ (Class metadata) │  │  Local variables, method calls    │  │
│  └──────────────────┘  └────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────┐  ┌────────────────────────────────────┐  │
│  │   Code Cache     │  │      Native Method Stack          │  │
│  │ (JIT compiled)   │  │                                    │  │
│  └──────────────────┘  └────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Heap vs Stack

### Stack Memory
- **Thread-specific** - each thread has its own stack
- **LIFO** (Last In, First Out) structure
- **Stores**: local variables, method parameters, return addresses
- **Fast allocation/deallocation**
- **Size limited** (StackOverflowError if exceeded)

```java
public void processPost(Long id) {      // id stored on stack
    int count = 0;                       // count on stack
    Post post = postService.find(id);   // post reference on stack
                                         // Post object on HEAP
}
```

### Heap Memory
- **Shared** across all threads
- **Stores**: all objects and class instances
- **Managed by Garbage Collector**
- **Larger size**, configurable with -Xms and -Xmx

```java
Post post = new Post();  // 'post' reference → Stack
                         // Post object → Heap
```

### Comparison

| Aspect | Stack | Heap |
|--------|-------|------|
| Access | Thread-only | All threads |
| Speed | Very fast | Slower |
| Size | Limited | Large, configurable |
| Memory management | Automatic (LIFO) | Garbage Collector |
| Data | Primitives, references | Objects |

---

## Garbage Collection

### What is GC?
Automatic memory management that reclaims memory used by objects that are no longer reachable.

### Object Lifecycle

```
1. Object created → Allocated in Eden space
2. Minor GC → Surviving objects move to Survivor space
3. After multiple GCs → Object promoted to Old Gen
4. Major/Full GC → Old Gen collected
```

### GC Roots
Objects that are always reachable:
- Local variables in stack frames
- Active threads
- Static variables
- JNI references

### Mark and Sweep Algorithm

```
MARK PHASE:
┌──────────┐
│ GC Roots │──→ Reachable objects marked
└──────────┘

SWEEP PHASE:
Unmarked (unreachable) objects → Freed
```

---

## Types of Garbage Collectors

### 1. Serial GC
```bash
-XX:+UseSerialGC
```
- Single-threaded
- Stop-the-world pauses
- Best for: Small applications, single CPU

### 2. Parallel GC (Throughput)
```bash
-XX:+UseParallelGC
```
- Multi-threaded
- Good throughput
- Best for: Batch processing, backend

### 3. G1 GC (Default since Java 9)
```bash
-XX:+UseG1GC
```
- Region-based
- Predictable pause times
- Best for: Large heaps, low latency

### 4. ZGC (Java 11+)
```bash
-XX:+UseZGC
```
- Very low latency (< 10ms pauses)
- Scalable to TB heaps
- Best for: Large-scale, latency-sensitive

### 5. Shenandoah (Java 12+)
```bash
-XX:+UseShenandoahGC
```
- Concurrent
- Ultra-low pause times
- Best for: Large heaps, consistent latency

---

## Memory Configuration

### JVM Memory Options

```bash
# Initial heap size
-Xms512m

# Maximum heap size
-Xmx2g

# Young generation size
-Xmn256m

# Metaspace size
-XX:MetaspaceSize=128m
-XX:MaxMetaspaceSize=256m

# Stack size per thread
-Xss512k
```

### Spring Boot Example

```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar blog-api.jar
```

Or in `application.yaml`:
```yaml
# These are set as environment variables or JVM args, not in yaml
# JAVA_OPTS: -Xms512m -Xmx2g
```

---

## Memory Leaks

### Common Causes
1. **Static collections holding references**
2. **Unclosed resources** (connections, streams)
3. **Listeners not removed**
4. **ThreadLocal not cleaned**

### Prevention

```java
// Use try-with-resources
try (Connection conn = dataSource.getConnection()) {
    // ...
}

// Clear collections
cache.clear();

// Remove listeners
eventSource.removeListener(listener);

// Clean ThreadLocal
threadLocal.remove();
```

---

## Monitoring & Profiling

### JVM Flags for GC Logging

```bash
# Java 11+
-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m
```

### Tools
- **jconsole** - JMX-based monitoring
- **jvisualvm** - Visual profiling
- **jstat** - GC statistics
- **jmap** - Heap dumps
- **Eclipse MAT** - Memory analyzer

```bash
# GC statistics
jstat -gc <pid> 1000

# Heap dump
jmap -dump:format=b,file=heap.hprof <pid>
```
