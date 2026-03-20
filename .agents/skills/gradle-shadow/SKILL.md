---
name: gradle-shadow
description: Working with Gradle Shadow plugin for creating fat JARs with dependency shading. Use when configuring ShadowJar tasks, relocate packages, include/exclude dependencies, minimize JARs, or troubleshooting shadow plugin issues.
---

<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# Gradle Shadow Plugin Skill

Guidance for using the Gradle Shadow plugin to create fat JARs with relocated
dependencies.

## Common Issues

### minimize() NullPointerException

**Error:** `Cannot read field "forJava" because "parsedFileName" is null`

**Cause:** The shadow plugin's `minimize()` feature uses jdependency library to
analyze class usage. When certain JAR files have unusual filenames or metadata,
jdependency fails to parse them, resulting in a NPE.

**When it happens:**

- After dependency updates that bring in new JAR files
- With certain dependencies that have non-standard packaging
- When minimize-related configurations contain problematic artifacts

**Fix:** Remove `minimize()` from your ShadowJar configuration:

```kotlin
// BEFORE (may cause NPE)
tasks.withType<ShadowJar>().configureEach {
  minimize()
}

// AFTER (stable)
tasks.withType<ShadowJar>().configureEach {
  // Use include/exclude filters instead
  dependencies {
    include { it.moduleGroup == "com.example" }
  }
}
```

## Configuration Patterns

### Basic Shadow with Relocation

```kotlin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("org.eclipse.jgit", "com.mycompany.shaded.jgit")
  relocate("com.google.common", "com.mycompany.shaded.guava")
}
```

### Selective Dependency Inclusion

Only include specific dependencies in the shadow JAR:

```kotlin
tasks.withType<ShadowJar>().configureEach {
  dependencies {
    include { it.moduleGroup == "com.example" && it.moduleName == "library" }
    include { it.moduleGroup == "com.google.guava" }
  }
}
```

### Selective Dependency Exclusion

Exclude specific dependencies (include everything else):

```kotlin
tasks.withType<ShadowJar>().configureEach {
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "some-library" }
  }
}
```

### Relocation Patterns by Module

Common relocation patterns for popular libraries:

| Original Package     | Relocated Package              |
| -------------------- | ------------------------------ |
| `org.eclipse.jgit`   | `com.mycompany.shaded.jgit`    |
| `com.google.common`  | `com.mycompany.shaded.guava`   |
| `org.apache.commons` | `com.mycompany.shaded.commons` |

## Gradle Plugin Portal Publishing

When publishing a Gradle plugin with shadow:

1. Set `archiveClassifier.set("")` to make shadow JAR the main artifact
2. The plugin-publish plugin automatically detects and uses the fat JAR
3. Relocate all dependencies to avoid classpath conflicts

```kotlin
gradlePlugin {
  plugins {
    register("my.plugin.id") {
      implementationClass = "com.mycompany.MyPlugin"
    }
  }
}
```

## Dependency Locking with Shadow

If using dependency locking with the shadow plugin, note that it may create
additional configurations (e.g., `shadow`, `shadowMinimizeApi`). After updating
dependencies, verify lockfiles include these configurations:

```bash
./gradlew dependencies --write-locks
```

## Best Practices

1. **Always relocate** - Prevent classpath conflicts by relocating shaded packages
2. **Use include over minimize** - If minimize() causes issues, use explicit
   include filters
3. **Check lockfile diffs** - After dependency updates, review lockfile changes
   for shadow-related configurations
4. **Test the shadow JAR** - Verify the fat JAR works in integration tests
