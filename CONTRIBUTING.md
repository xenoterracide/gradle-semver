<!--
SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# Contributing to gradle-semver

This is a **Gradle plugin project** that provides semantic versioning based on git history. It contains two plugins:

1. **`com.xenoterracide.gradle.semver`** (module: `semver`): A semantic versioning plugin that derives version from git tags and commits.
2. **`com.xenoterracide.gradle.git`** (module: `git`): Provides git metadata (branch, commit, tags, distance) to builds.

## Prerequisites

- Java 25+ (Temurin recommended)
- Node.js 24+ (via asdf or other version manager)
- Python 3.14+ (for REUSE compliance)

### Tooling

- **asdf**: Version manager for Java, Node.js, Python (see `.tool-versions`)
- **direnv**: Auto-loads environment when entering the directory (see `.envrc`)

## Initial Setup

```bash
# Enable Corepack for Yarn
corepack enable

# Install Node dependencies
yarn install --immutable --inline-builds --check-resolutions

# Run postinstall (sets up git hooks)
yarn run -T postinstall

# Verify Gradle dependencies
./gradlew dependencies
```

### GitHub Packages Authentication

Some dependencies are hosted on GitHub Packages. Create `~/.gradle/gradle.properties`:

```properties
ghUsername=<your username>
ghPassword=<your token>
```

Generate a PAT with at least `read:packages` scope as [documented here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages).

## Build Commands

### Gradle Commands

```bash
./gradlew build                      # Build the project
./gradlew check                      # Run all checks (tests, SpotBugs, Checkstyle)
./gradlew test                       # Run unit tests only
./gradlew testIntegration            # Run integration tests
./gradlew compile                    # Compile all source sets (lifecycle task from convention plugin)
./gradlew checkstyle                 # Run checkstyle on all source sets (lifecycle task from convention plugin)
./gradlew dependencies --write-locks # Update dependency locks
./gradlew buildHealth                # Check dependency health
./gradlew semverVersion              # Print computed semantic version
./gradlew version                    # Print project version
./gradlew publishToMavenLocal        # Publish to local Maven cache for testing
```

### Yarn Scripts

```bash
yarn test          # Run all checks (./gradlew check)
yarn cleaner       # Clean build directories
yarn ug            # Update dependency locks
yarn ug:dogfood    # Update locks with --refresh-dependencies
yarn merge         # Run full merge workflow (default: junie engine)
yarn merge:kimi    # Run merge workflow with kimi
yarn merge:copilot # Run merge workflow with copilot
```

## Code Style

### Java

- **Formatter**: Prettier with `prettier-plugin-java`
- **Static Analysis**: ErrorProne, SpotBugs, Checkstyle
- **Null Safety**: Use JSpecify annotations (`@Nullable`, `@NonNull`)
- **Line Length**: 120 characters

### Kotlin DSL

- **Formatter**: ktlint 1.8.0
- Run: `ktlint '*.gradle.kts' '**/*.gradle.kts'`

### General Formatting

- **EditorConfig**: 2-space indentation, LF line endings, UTF-8
- **Prettier**: Handles XML, YAML, JSON, TOML, properties, shell scripts
- **Kotlin DSL**: Handled by ktlint (not Prettier)

## Git Hooks

Hooks are in `.share/git/hooks/` and are configured automatically by `yarn contribute`.

- **pre-commit**: Runs `lint-staged` (formatting + license annotation)
- **commit-msg**: Validates conventional commit format
- **prepare-commit-msg**: AI-assisted commit message generation

## Commit Conventions

Use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/): `type(scope): subject`

### Allowed Types

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
- `ops`: Operations
- `merge`: Merge commits
- `revert`: Reverts

## Testing

Each module has three test source sets:

1. **`src/test`**: Unit tests using JUnit 5 + AssertJ
2. **`src/testFixtures`**: Shared test utilities
3. **`src/testIntegration`**: Gradle TestKit integration tests

## Release Process

```bash
git tag -m "v0.12.1" -a v0.12.1 && git push --tags
```

## License

All licenses are documented explicitly using SPDX identifiers:

- **Java**: [GPLv3](https://choosealicense.com/licenses/gpl-3.0/) with [Classpath Exception](https://spdx.org/licenses/Classpath-exception-2.0.html)
- **Gradle/Kotlin/Config**: [MIT](https://choosealicense.com/licenses/mit/)
- **Documentation/Javadoc**: [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)
