<!--
SPDX-FileCopyrightText: Copyright © 2025 - 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-4.0
-->

# Semver Plugin

A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe.

_Plugin ID_: `"com.xenoterracide.gradle.semver"`
_Plugin GAV_: `"com.xenoterracide.gradle:semver:0.15.+"
_Version_: `0.15.+`

## Usage

```kt
plugins {
  id("com.xenoterracide.gradle.semver")
}

version = semver
```

This is the simplest way to get your semver, but I don't recommend it
because [Gradle isn't lazy with anything related to publishing](https://github.com/gradle/gradle/issues/29342). Even
when it becomes lazy I doubt it'll be as lazy as you want. So I do the following, and then only set `IS_PUBLISHING` in
my publishing build in CI. This avoids constant configuration cache busting as well as ensuring that IO is kept to a
minimum. The `semver.provider` should never have a `null` result under any circumstances; if it does that is a bug.

```kt
import org.semver4j.Semver

version =
  providers
    .environmentVariable("IS_PUBLISHING")
    .flatMap { semver.provider }
    .getOrElse(Semver.ZERO)
```

if you want you can expose whether your tree is dirty or not.

```kt
semver {
  checkDirty.set(true) // expensive since it invalidates the configuration cache every change
}

logger.quiet("semver: " + semver) // 0.1.1-alpha.0.1+branch.topic-foo.git.32.3aae11e.dirty
```

The plugin exposes a `Semver`. See [Semver4J](https://javadoc.io/doc/org.semver4j/semver4j/latest/index.html).

### Tasks

#### `./gradlew semverVersion`

Prints the semantic version computed by the `com.xenoterracide.gradle.semver` plugin.

- Output is always a single line.
- This value is derived from git metadata (tags, distance, branch, dirty status), _not_ from `project.version`.
- In a repo without a usable git history/tag, it falls back to `0.0.0-alpha.0.0`.

Examples:

```sh
./gradlew semverVersion --quiet
# 0.1.0

./gradlew semverVersion --quiet
# 0.0.0-alpha.0.0
```

#### `./gradlew version`

Prints `project.version`.

This task is intentionally conservative because end users may or may not assign a value to `project.version`.

- If `project.version` is unset (Gradle’s default `unspecified`), the task prints **nothing** (just a newline).
- If `project.version` is set, it prints that value.

For scripting, you usually want `--quiet`:

```sh
./gradlew version --quiet
# (prints a newline only when project.version is unset)

./gradlew -Pversion=1.2.3 version --quiet
# 1.2.3
```
