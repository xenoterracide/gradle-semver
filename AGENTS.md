<!--
SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# AGENTS.md - AI Context for gradle-semver

This file contains AI-specific context. For general contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Project Overview

This is a **Gradle plugin project** providing semantic versioning derived from git history. It contains two plugins:

1. **`com.xenoterracide.gradle.semver`** (module: `semver`): Semantic versioning plugin that calculates versions from git tags and commits
2. **`com.xenoterracide.gradle.git`** (module: `git`): Git metadata provider plugin exposing branch, commit, tags, and distance information

The plugins expect git tags in the format `v0.1.1` (annotated tags) and prerelease versions like `v0.1.1-rc.1`.

## Technology Stack

| Component | Version/Tool |
|-----------|--------------|
| **Language** | Java 17+ (source/target), Java 21 (toolchain) |
| **Build System** | Gradle 9.3+ with Kotlin DSL |
| **Task Runner** | Yarn 4.x (package manager for Node tooling) |
| **Version Management** | asdf (`.tool-versions`) |
| **Git Hooks** | lint-staged, git-conventional-commits |
| **Python** | 3.14+ (for REUSE compliance) |

### Key Dependencies

- **Eclipse JGit**: Git operations
- **Semver4j**: Semantic version parsing and manipulation
- **Vavr**: Functional programming utilities
- **Guava**: Google's core libraries
- **Immutables**: Annotation processor for immutable value objects
- **JUnit 5 + AssertJ**: Testing framework

## Project Structure

```
gradle-semver/
├── module/
│   ├── git/                    # Git metadata plugin
│   │   ├── src/main/java/      # Plugin implementation
│   │   ├── src/test/java/      # Unit tests
│   │   ├── src/testFixtures/   # Shared test utilities
│   │   └── src/testIntegration/# Gradle TestKit integration tests
│   └── semver/                 # Semantic versioning plugin
│       ├── src/main/java/      # Plugin implementation
│       ├── src/test/java/      # Unit tests
│       └── src/testIntegration/# Integration tests
├── buildSrc/                   # Build logic convention plugins
│   └── src/main/kotlin/
│       └── our.convention.gradle.kts  # Shared build configuration
├── .agents/skills/             # AI agent skills
├── .share/git/hooks/           # Git hooks
├── .github/workflows/          # CI/CD workflows
├── .config/                    # Tool configurations
└── gradle/                     # Gradle wrapper
```

### Module: `git`

Provides git metadata through the `GitExtension`:

- `branch`: Current branch name
- `commit`: Full commit SHA
- `uniqueShort`: Short unique commit SHA
- `tag`: Latest tag
- `distance`: Commit distance from tag
- `status`: Repository status (clean/dirty)

Key classes:
- `GitPlugin`: Plugin entry point
- `GitMetadata`: Interface for git metadata
- `GitService`: Service for git operations using JGit
- `DistanceCalculator`: Calculates commit distance between references

### Module: `semver`

Calculates semantic versions using strategy pattern:

| Scenario | HEAD Branch Output | Topic Branch Output |
|----------|-------------------|---------------------|
| On exact tag | `1.0.0` | `1.0.0+branch.feature.git.0.abc123` |
| After stable tag | `1.0.1-alpha.0.5+git.5.abc123` | `1.0.1-alpha.0.5+branch.feature.git.3.abc123` |
| After pre-release tag | `1.0.0-rc.1.5+git.5.abc123` | `1.0.0-rc.1.5+branch.feature.git.3.abc123` |
| No tags | `0.0.1-alpha.0.5+git.5.abc123` | `0.0.1-alpha.0.5+branch.feature.git.3.abc123` |

Key classes:
- `SemverPlugin`: Plugin entry point
- `VersionStrategy`: Strategy interface for version calculation
- `SemverExtension`: Extension for configuring the plugin
- `PrintVersionTask`: Task to print computed version

## Build System

### Gradle Configuration

- **Settings**: `settings.gradle.kts` - Multi-module project with dynamic module discovery from `module/` directory
- **Root build**: `build.gradle.kts` - Applies dependency analysis, sets up version from semver provider
- **Convention Plugin**: `buildSrc/src/main/kotlin/our.convention.gradle.kts` - Shared build logic for all modules

### Convention Plugin Features

- Java toolchain (Java 21)
- Source/target compatibility (Java 17)
- ErrorProne static analysis
- SpotBugs security analysis
- Checkstyle code style checks
- JaCoCo code coverage
- Javadoc generation
- Gradle TestKit integration testing
- Maven publication to GitHub Packages and Gradle Plugin Portal

### Build Commands

```bash
# Gradle commands
./gradlew build                      # Build all modules
./gradlew check                      # Run all checks (tests, SpotBugs, Checkstyle)
./gradlew test                       # Run unit tests only
./gradlew testIntegration            # Run integration tests
./gradlew checkstyle                 # Run checkstyle on all source sets
./gradlew dependencies --write-locks # Update dependency locks
./gradlew buildHealth                # Check dependency health
./gradlew semverVersion              # Print computed semantic version
./gradlew version                    # Print project version
./gradlew publishToMavenLocal        # Publish to local Maven cache

# Yarn scripts
yarn test          # Run all checks (./gradlew check)
yarn cleaner       # Clean build directories
yarn ug            # Update dependency locks
yarn merge         # Run full merge workflow (Makefile)
```

## Testing Strategy

Each module has three test source sets:

1. **`src/test`** - Unit tests (JUnit 5 + AssertJ)
   - Fast, isolated tests for individual classes
   - Located in `src/test/java`

2. **`src/testFixtures`** - Shared test utilities
   - Shared test helpers and fixtures
   - Available to other modules via `testFixtures(project())`
   - Example: `CommitTools` in git module for creating test git repositories

3. **`src/testIntegration`** - Gradle TestKit integration tests
   - End-to-end tests using Gradle TestKit
   - Tests plugin application and task execution
   - Located in `src/testIntegration/java`

### Running Tests

```bash
./gradlew test                       # Unit tests only
./gradlew testIntegration            # Integration tests only
./gradlew check                      # All tests + quality checks
```

## Code Style and Quality

### Java

- **Formatter**: Prettier with `prettier-plugin-java`
- **Line Length**: 120 characters
- **Null Safety**: JSpecify annotations (`@Nullable`, `@NonNull`)
- **Static Analysis**:
  - ErrorProne (compile-time bug detection)
  - SpotBugs (bytecode analysis)
  - Checkstyle (style enforcement)

### Kotlin DSL

- **Formatter**: ktlint 1.8.0
- Run: `ktlint '*.gradle.kts' '**/*.gradle.kts'`

### General Formatting

- **EditorConfig**: 2-space indentation, LF line endings, UTF-8 (see `.editorconfig`)
- **Prettier**: Handles XML, YAML, JSON, TOML, properties, shell scripts
- **License Headers**: Managed via `lint-staged` and REUSE

### Pre-commit Hooks

Hooks are in `.share/git/hooks/` and configured via `yarn contributor`:

- **pre-commit**: Runs `lint-staged` (formatting + license annotation)
- **commit-msg**: Validates conventional commit format
- **prepare-commit-msg**: AI-assisted commit message generation

## Development Workflow

### Conventional Commits

Format: `type(scope): subject`

Allowed types (from `git-conventional-commits.yaml`):
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting (no code change)
- `refactor`: Code restructuring
- `perf`: Performance improvement
- `test`: Tests
- `build`: Build system
- `ci`: CI/CD changes
- `chore`: Maintenance
- `deps`: Dependencies

### Merge Workflow (Makefile)

```bash
yarn merge          # Default (junie engine)
yarn merge:kimi     # Using kimi engine
yarn merge:copilot  # Using copilot engine
```

The merge workflow:
1. Fetches and merges origin/HEAD
2. Pushes current branch
3. Creates/updates PR with AI-generated message
4. Waits for build workflow to pass
5. Interactive prompt for squash merge

## License Compliance (REUSE 3.0)

All files must have SPDX license identifiers. Licenses per file type:

| File Type | License |
|-----------|---------|
| Java source | GPL-3.0-or-later WITH Classpath-exception-2.0 |
| Gradle/Kotlin build scripts | MIT |
| Documentation | CC-BY-NC-SA-4.0 |
| Config/Data files | CC0-1.0 |

### REUSE Commands

```bash
reuse lint                           # Check compliance
reuse annotate --license ...         # Add license header
```

Lint-staged automatically adds license headers based on file type (see `.lintstagedrc.yml`).

## CI/CD (GitHub Actions)

### Workflows

1. **`build.yml`** - Main build workflow
   - Runs on all pushes and tags
   - Jobs: build, full (no cache), publish
   - Publishes to GitHub Packages and Gradle Plugin Portal on tags

2. **`pre-commit.yml`** - Quality checks
   - License compliance (`reuse lint`)
   - Prettier formatting check
   - ktlint for Kotlin DSL

3. **`update-java.yml`** - Automated dependency updates

### Required Secrets

- `GRADLE_ENCRYPTION_KEY`: Cache encryption
- `GPG_SECRET_KEY` / `GPG_PASSPHRASE`: Artifact signing
- `GRADLE_PUBLISH_KEY` / `GRADLE_PUBLISH_SECRET`: Gradle Plugin Portal

## Publishing

### Repositories

1. **GitHub Packages**: `https://maven.pkg.github.com/xenoterracide/gradle-semver`
2. **Gradle Plugin Portal**: `https://plugins.gradle.org/`

### Release Process

```bash
# Create annotated tag (triggers publish workflow)
git tag -m "v0.12.1" -a v0.12.1 && git push --tags
```

Release workflow:
1. Builds and tests
2. Publishes to staging repository
3. Creates GitHub release with archives
4. Publishes to Gradle Plugin Portal (tags only)

## Dependency Management

- **Lock Files**: All configurations are locked (`*.lockfile`)
- **Update Command**: `./gradlew dependencies --write-locks` or `yarn ug`
- **Dogfood Update**: `yarn ug:dogfood` (with `--refresh-dependencies`)

### Authentication for GitHub Packages

Create `~/.gradle/gradle.properties`:
```properties
ghUsername=<your username>
ghPassword=<your token>
```

Token needs `read:packages` scope minimum.

## Security Considerations

1. **Dependency Locking**: All dependencies are locked to prevent supply chain attacks
2. **SpotBugs**: Security vulnerability scanning in bytecode
3. **Dependency Analysis**: Automated dependency health checks via `buildHealth`
4. **Secret Scanning**: Integrated with GitHub secret scanning
5. **GPG Signing**: All published artifacts are signed
6. **Configuration Cache**: Gradle configuration cache enabled for faster, reproducible builds

## Troubleshooting

### Shallow Clones

Shallow clones break version distance calculation. Use instead:
```bash
git fetch --all --filter blob:none
```

### GitHub Actions Annotated Tags

GitHub doesn't checkout annotated tags properly. Workaround in workflow:
```yaml
- uses: actions/checkout@v4
  with:
    ref: ${{ github.ref }}
```

### Version Shows 0.0.0

- Ensure git tags exist in the format `v0.1.0`
- Ensure full git history is available (not shallow clone)
- Run `git remote set-head --auto origin` to set HEAD branch

## Project-Specific Conventions

1. **Package Naming**: `com.xenoterracide.gradle.<module>`
2. **Plugin IDs**: `com.xenoterracide.gradle.<module>`
3. **Group ID**: `com.xenoterracide.gradle`
4. **Version Tags**: Always annotated tags with `v` prefix (e.g., `v0.15.0`)
5. **Branch Naming**: Uses `HEAD` branch concept from `git remote set-head`

## Available Tools

- `gh` - GitHub CLI (for PR operations, workflows)
- `reuse` - REUSE compliance tool
- `ktlint` - Kotlin formatter
- `prettier` - Multi-language formatter
- `git-conventional-commits` - Commit message validation
