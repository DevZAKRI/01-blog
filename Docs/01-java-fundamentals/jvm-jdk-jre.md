# JVM + JDK + JRE Explained

## Overview

```
┌─────────────────────────────────────────────────────────────┐
│                          JDK                                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                        JRE                            │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │                      JVM                        │  │  │
│  │  │   • Class Loader                                │  │  │
│  │  │   • Runtime Data Areas                          │  │  │
│  │  │   • Execution Engine                            │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                       │  │
│  │  + Core Libraries (java.lang, java.util, etc.)        │  │
│  │  + Other Libraries (java.sql, java.net, etc.)         │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  + Development Tools (javac, jar, javadoc, jdb, etc.)       │
│  + Source Code                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## JVM (Java Virtual Machine)

The JVM is an abstract computing machine that enables Java bytecode to run on any platform.

### Key Responsibilities
1. **Load bytecode** (.class files)
2. **Verify** bytecode for security
3. **Execute** bytecode
4. **Provide runtime environment**

### JVM Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Class Loader Subsystem                   │
│         Loading → Linking → Initialization                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Runtime Data Areas                        │
│  ┌─────────────┐ ┌─────────────┐ ┌───────────────────────┐  │
│  │   Method    │ │    Heap     │ │   Java Stacks         │  │
│  │   Area      │ │ (Objects)   │ │ (per thread)          │  │
│  └─────────────┘ └─────────────┘ └───────────────────────┘  │
│  ┌─────────────┐ ┌─────────────────────────────────────────┐│
│  │  PC Reg     │ │         Native Method Stacks           ││
│  └─────────────┘ └─────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Execution Engine                         │
│    Interpreter  │  JIT Compiler  │  Garbage Collector       │
└─────────────────────────────────────────────────────────────┘
```

### Write Once, Run Anywhere (WORA)

```
  Source Code          Bytecode             Machine Code
     .java      →       .class      →      Native Code
   (compile)           (JVM)             (JIT compile)
     
  Platform-           Platform-          Platform-
  Independent         Independent        Specific
```

---

## JRE (Java Runtime Environment)

The JRE is the minimum environment needed to **run** Java applications.

### Components
- **JVM** - Executes bytecode
- **Core Libraries** - java.lang, java.util, java.io, etc.
- **Supporting Files** - Configuration, property files

### When to Use
- End users running Java applications
- Servers running Java-based applications
- **Not for development** (no compiler)

---

## JDK (Java Development Kit)

The JDK is the full development kit needed to **develop and run** Java applications.

### Components
- **JRE** - Everything in JRE
- **Compiler (javac)** - Compiles .java to .class
- **Tools**:
  - `jar` - Create and manage JAR files
  - `javadoc` - Generate documentation
  - `jdb` - Debugger
  - `jconsole` - Monitoring tool
  - `jshell` - Interactive REPL (Java 9+)

### When to Use
- Developers writing Java code
- Build servers compiling Java applications
- Anyone who needs to compile Java code

---

## Comparison Table

| Feature | JVM | JRE | JDK |
|---------|-----|-----|-----|
| Run Java programs | ✓ | ✓ | ✓ |
| Core libraries | ✗ | ✓ | ✓ |
| Development tools | ✗ | ✗ | ✓ |
| Compiler (javac) | ✗ | ✗ | ✓ |
| Debugger (jdb) | ✗ | ✗ | ✓ |

---

## Java Compilation Process

```
┌──────────────────┐
│  HelloWorld.java │
│  (Source Code)   │
└────────┬─────────┘
         │ javac (compiler)
         ▼
┌──────────────────┐
│ HelloWorld.class │
│   (Bytecode)     │
└────────┬─────────┘
         │ java (JVM)
         ▼
┌──────────────────┐
│   JVM loads &    │
│   executes       │
└──────────────────┘
```

---

## JDK Versions & Vendors

### LTS (Long Term Support) Versions
- Java 8 (2014)
- Java 11 (2018)
- Java 17 (2021)
- Java 21 (2023)

### Popular JDK Vendors
| Vendor | Distribution |
|--------|--------------|
| Oracle | Oracle JDK, OpenJDK |
| Amazon | Corretto |
| Eclipse | Temurin (AdoptOpenJDK) |
| Azul | Zulu |
| Red Hat | Red Hat OpenJDK |

---

## Practical Commands

```bash
# Check Java version
java -version

# Check compiler version
javac -version

# Compile Java file
javac HelloWorld.java

# Run Java program
java HelloWorld

# Create JAR file
jar cvf app.jar *.class

# Run JAR file
java -jar app.jar

# Interactive shell (Java 9+)
jshell
```
