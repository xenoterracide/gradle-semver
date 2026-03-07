<!--
SPDX-FileCopyrightText: Copyright © 2025, 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# Semver Plugin

A [semantic versioning](https://semver.org/) plugin that derives the version from git tags and commits and is configuration cache safe.

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

## Version Calculation Algorithm

The plugin uses a strategy pattern to determine the version based on git context:

### Scenarios

| Scenario                                        | HEAD Branch Output             | Topic Branch Output                           |
| ----------------------------------------------- | ------------------------------ | --------------------------------------------- |
| **On exact tag**                                | `1.0.0`                        | `1.0.0+branch.feature.git.0.abc123`           |
| **After stable tag** (e.g., `v1.0.0`)           | `1.0.1-alpha.0.5+git.5.abc123` | `1.0.1-alpha.0.5+branch.feature.git.3.abc123` |
| **After pre-release tag** (e.g., `v1.0.0-rc.1`) | `1.0.0-rc.1.5+git.5.abc123`    | `1.0.0-rc.1.5+branch.feature.git.3.abc123`    |
| **No tags in repo**                             | `0.0.1-alpha.0.5+git.5.abc123` | `0.0.1-alpha.0.5+branch.feature.git.3.abc123` |

### Terminology

- **HEAD branch**: The default branch of the remote (e.g., `main`, `develop`, `master`).
  See [`git remote set-head`](https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem).
- **Merge base**: The best common ancestor between two commits (where the topic branch diverged).
  See [`git merge-base`](https://git-scm.com/docs/git-merge-base).
- **Distance**: Number of commits between two points (computed via `git rev-list --count`).

### Key Behaviors

1. **HEAD Branch vs Topic Branch**:
   - On the HEAD branch, versions include `+git.<distance>.<sha>` [build metadata](https://semver.org/#spec-item-10)
   - On topic branches, versions include `+branch.<name>.git.<distance>.<sha>` build metadata
   - On exact tags (any branch): Clean version without build metadata

2. **Distance Calculation**:
   - **Prerelease distance**: Distance from tag (same for HEAD and topic branches)
   - **Build metadata distance**: For HEAD branch, distance from tag; for topic branch, distance from merge base

3. **Stable vs Pre-release Tags**:
   - After a stable tag (e.g., `v1.0.0`): Patch is incremented (`1.0.1-alpha...`)
   - After a pre-release tag (e.g., `v1.0.0-rc.1`): Distance appended (`1.0.0-rc.1.3`)

4. **Dirty Working Tree**:
   - When `checkDirty` is enabled, `.dirty` is appended to metadata
   - Example: `0.1.1-alpha.0.3+branch.feature.git.3.abc123.dirty`

5. **No Tags**:
   - Starts from `0.0.0` base, resulting in `0.0.1-alpha...`

### Version Components

```
<major>.<minor>.<patch>[-<prerelease>][+<metadata>]
```

- **major.minor.patch**: From the nearest tag (or `0.0.0` if no tags)
- **prerelease**: `alpha.0.<distance>` for stable tags, or `<tag-prerelease>.<distance>` for pre-release tags
- **metadata**:
  - HEAD branch: `git.<distance>.<short-sha>`
  - Topic branch: `branch.<branch-name>.git.<distance>.<short-sha>`
  - Dirty: `.dirty` appended to metadata

## Tasks

### `./gradlew semverVersion`

Prints the semantic version computed by the `com.xenoterracide.gradle.semver` plugin.

- Output is always a single line.
- This value is derived from git metadata (tags, distance, branch, dirty status), _not_ from `project.version`.
- In a repo without a usable git history/tag, it falls back to `0.0.0`.

Examples:

```sh
./gradlew semverVersion --quiet
# 0.1.0

./gradlew semverVersion --quiet
# 0.0.0
```

### `./gradlew version`

Prints `project.version`.

This task is intentionally conservative because end users may or may not assign a value to `project.version`.

- If `project.version` is unset (Gradle's default `unspecified`), the task prints **nothing** (no output).
- If `project.version` is set, it prints that value.

For scripting, you usually want `--quiet`:

```sh
./gradlew version --quiet
# (prints nothing when project.version is unset)

./gradlew -Pversion=1.2.3 version --quiet
# 1.2.3
```
