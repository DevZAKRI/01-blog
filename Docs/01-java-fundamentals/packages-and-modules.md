# Packages & Modules

## Packages

Packages are namespaces that organize classes and interfaces into logical groups.

### Purpose
- **Avoid naming conflicts**
- **Access control**
- **Logical organization**

### Package Declaration

```java
package com.zerooneblog.blog.service;

import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.repository.PostRepository;
```

### Package Naming Conventions
- Use lowercase letters
- Reverse domain name (e.g., `com.company.project`)
- Reflect the project structure

### Common Package Structure
```
com.zerooneblog.blog
├── controller    # REST endpoints
├── service       # Business logic
├── repository    # Data access
├── model         # Entities
├── dto           # Data Transfer Objects
├── config        # Configuration classes
└── exception     # Custom exceptions
```

---

## Modules (Java 9+)

Modules are higher-level aggregations of packages introduced in Java 9 (Project Jigsaw).

### module-info.java

```java
module com.zerooneblog.blog {
    requires spring.boot;
    requires spring.data.jpa;
    
    exports com.zerooneblog.blog.api;
    exports com.zerooneblog.blog.dto;
    
    opens com.zerooneblog.blog.model to hibernate.core;
}
```

### Module Keywords

| Keyword | Purpose |
|---------|---------|
| `requires` | Declares dependency on another module |
| `exports` | Makes package available to other modules |
| `opens` | Allows reflection access |
| `provides` | Declares service implementation |
| `uses` | Declares service consumption |

---

## Access Modifiers with Packages

| Modifier | Class | Package | Subclass | World |
|----------|-------|---------|----------|-------|
| public | ✓ | ✓ | ✓ | ✓ |
| protected | ✓ | ✓ | ✓ | ✗ |
| default (package-private) | ✓ | ✓ | ✗ | ✗ |
| private | ✓ | ✗ | ✗ | ✗ |

---

## Best Practices

1. **One public class per file**
2. **Package by feature, not by layer** (optional but recommended)
3. **Keep packages cohesive**
4. **Use meaningful names**
