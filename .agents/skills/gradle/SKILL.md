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

## Dependency Management

### Checking for Lockfiles

Look for these files to determine if dependency locking is enabled:

- `gradle.lockfile` (project dependencies)
- `buildscript-gradle.lockfile` (buildscript classpath)
- `*/gradle.lockfile` (module-specific)
- `*/buildscript-gradle.lockfile` (module-specific buildscript)

### Updating Dependencies

If dependency locking is enabled, update lockfiles after changing dependencies:

```bash
./gradlew dependencies --write-locks
```

To force refresh before updating:

```bash
./gradlew dependencies --refresh-dependencies --write-locks
```

### Analyzing Dependencies

When investigating dependency issues:

1. **Check lockfile diffs** - Review git changes to `*.lockfile` files
2. **Look for configuration changes** - Dependency updates may add/remove
   configurations
3. **Verify consistency** - Ensure all lockfiles are updated together

### Viewing Dependency Trees

```bash
./gradlew dependencies
./gradlew dependencies --configuration runtimeClasspath
```

## Common Build Tasks

```bash
./gradlew build          # Build and test
./gradlew check          # Run all checks (tests, linting)
./gradlew test           # Run unit tests
./gradlew clean          # Clean build outputs
```
