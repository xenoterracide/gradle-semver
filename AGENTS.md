<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# AGENTS.md - AI Context for gradle-semver

This file contains AI-specific context. For general contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Project Overview

This is a **Gradle plugin project** providing semantic versioning from git history. Contains two plugins:

1. **`com.xenoterracide.gradle.semver`** (module: `semver`): Semantic versioning plugin
2. **`com.xenoterracide.gradle.git`** (module: `git`): Git metadata provider

## AI-Specific Information

### Available Tools

- `gh` - GitHub CLI (for PR operations)

### Project-Specific Paths

- **Skills**: `.agents/skills/`
- **Git hooks**: `.share/git/hooks/`
- **Scripts**: `.share/bin/pr-message.sh`

### Build System

See CONTRIBUTING.md for full build instructions. Key points:

- **Language**: Java 17+ (toolchain: Java 21)
- **Build**: Gradle 9.x with Kotlin DSL
- **Task runner**: Yarn 4 (see `package.json` scripts)

### Testing

Each module has three test source sets:

1. `src/test` - Unit tests (JUnit 5 + AssertJ)
2. `src/testFixtures` - Shared test utilities
3. `src/testIntegration` - Gradle TestKit integration tests

### License Compliance (REUSE 3.0)

- **Java**: GPL-3.0-or-later WITH Classpath-exception-2.0
- **Gradle/Kotlin**: MIT
- **Documentation**: CC-BY-NC-SA-4.0
- **Config/Data**: CC0-1.0
