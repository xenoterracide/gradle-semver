<!--
SPDX-License-Identifier: CC-BY-4.0
© Copyright 2024 Caleb Cushing. All rights reserved.
-->

# README

A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe.

_Plugin ID_: `"com.xenoterracide.gradle.semver"`
_Version_: `0.9.+`

## Usage

_IMPORTANT_: this plugin will not work if you haven't run `git init` in your project.

```kt
plugins {
  id("com.xenoterracide.gradle.semver")
}

version = semver.maven
```

```kt
logger.quiet("maven:{}", semver.maven )
logger.quiet("gradlePlugin:{}", semver.gradlePlugin)
logger.quiet("branch:{}", semver.git.branch )
logger.quiet("commit:{}", semver.git.commit)
logger.quiet("commitShort:{}", semver.git.commitShort)
logger.quiet("latestTag:{}", semver.git.latestTag)
logger.quiet("describe:{}", semver.git.describe)
logger.quiet("commitDistance:{}", semver.git.commitDistance)
logger.quiet("dirty:{}", semver.git.dirty)
```

The plugin exposes a `Semver` from https://github.com/semver4j/semver4j and uses `Semver.coerce`.

- If no commits, or version tags have been made your version will be `0.0.0-SNAPSHOT`.
- If no valid tags are detected then your version will be `0.0.0-SNAPSHOT`.
- If a valid tag pattern `v\d+\.\d+\.\d+` matchs the current sha then your version will be that tag. e.g. `v1.0.0` translates to `1.0.0`
- If you've made commits after a valid version tag then your version will be `v\d+\.\d+\.\d+-SNAPSHOT-\d+-g\\p{XDigit}{7}`, e.g. `1.0.0-SNAPSHOT-1-g1abc234` You'll note that we've made one change from the semver4j library, we're subclassing in order to replace the `+` with `-` as that's what the maven version specification uses.

If you want you can do things like this

```kt
version = semver.version // almost same as version = semver because semver.toString() calls semver.version
semver.major
semver.minor
```

See [Semver](https://javadoc.io/doc/org.semver4j/semver4j/latest/index.html) for more methods.

## Goals

Provide Semantic versioning for Maven publishing with Gradle.

_Future_: Provide a way to determine what the next version should be using your projects ABI.

## Contributing

### Languages

[asdf](https://asdf-vm.com) is suggested, you can use whatever you'd like to get

- Java 11+
- NodeJs

add a way to export these to your `PATH` in your `~/.profile`

### Build Tools

- [Gradle](https://docs.gradle.org/current/userguide/command_line_interface.html)
- [NPM](https://docs.npmjs.com/about-npm)

Run `npm ci && ./gradlew dependencies` to install dependencies.

### Committing

Use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

## License

- Java: [Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)
- Gradle Kotlin and Config Files: [MIT](https://choosealicense.com/licenses/mit/)
- Documentation including Javadoc: [CC BY 4.0](https://choosealicense.com/licenses/cc-by-4.0/)

© Copyright 2024 Caleb Cushing. All rights reserved.
