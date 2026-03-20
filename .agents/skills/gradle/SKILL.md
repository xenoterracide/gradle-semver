---
name: gradle
description: Working with Gradle build system and Kotlin DSL. Use when editing build.gradle.kts, settings.gradle.kts, gradle.properties, or any Gradle configuration files. Also use when analyzing dependencies, updating versions, or troubleshooting Gradle build issues.
---

<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# Gradle Skill

Guidance for working with Gradle build system.

## Dependency Locking

If this project uses Gradle dependency locking (check for `*.lockfile` files):

**Lockfile locations:**

- `buildscript-gradle.lockfile` (root buildscript classpath)
- `gradle.lockfile` (root project dependencies)
- `*/buildscript-gradle.lockfile` (module-specific buildscript)
- `*/gradle.lockfile` (module-specific dependencies)

### When Analyzing Dependencies

When investigating dependency issues or version conflicts:

1. **Check lockfile changes** - Compare `*.lockfile` changes in git to see what
   versions changed
2. **Look for configuration changes** - Dependency updates may add/remove
   configurations
3. **Verify lockfiles are in sync** - After dependency changes, run:
   ```bash
   ./gradlew dependencies --write-locks
   ```

### Troubleshooting Shadow Plugin Issues

If this project uses the Shadow plugin, the `minimize()` feature can cause
issues with certain dependencies:

- **Error**: `Cannot read field "forJava" because "parsedFileName" is null`
- **Cause**: jdependency (used by minimize()) fails to parse certain JAR
  filenames
- **Fix**: Remove `minimize()` from the `ShadowJar` configuration if it causes
  issues

Example shadow configuration without minimize:

```kotlin
tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("com.example.lib", "my.shaded.lib")
  dependencies {
    include { it.moduleGroup == "com.example" }
  }
  // minimize() // Remove if causing NPE
}
```

## Dependency Updates

Standard workflow for updating dependencies (adjust commands based on project
setup):

```bash
# Update locks
./gradlew dependencies --write-locks

# Force refresh and update
./gradlew dependencies --refresh-dependencies --write-locks
```

After updates, verify build passes:

```bash
./gradlew check
```
