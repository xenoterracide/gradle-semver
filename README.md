<!--
SPDX-License-Identifier: CC-BY-4.0
© Copyright 2024 Caleb Cushing. All rights reserved.
-->

# README

A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe.

_Plugin ID_: `"com.xenoterracide.gradle.semver"`
_Version_: `0.9.+`

## Usage

```kt
plugins {
  id("com.xenoterracide.gradle.semver")
}

version = semver.maven
```

```kt
// given the last tag was v0.1.0 and you have a commit distance == 1 you'll get something like
logger.quiet("maven snapshot " + semver.mavenSnapshot)      // 0.1.1-SNAPSHOT
logger.quiet("maven alpha " + semver.mavenAlpha)            // 0.1.1-alpha.1001255204163142
logger.quiet("gradlePlugin " + semver.gradlePlugin)         // 0.1.1-alpha.1+1.g3aae11e

// other available outputs
logger.quiet("branch" + semver.git.branch)                 // main
logger.quiet("commit " + semver.git.commit)                 // 761c420fa9812584e90750ca73197402603e76cc
logger.quiet("commitShort " + semver.git.commitShort)       // g3aae11e
logger.quiet("latestTag " + semver.git.latestTag)           // v0.1.0
logger.quiet("describe " + semver.git.describe)             // v0.9.7-28-g55329c4
logger.quiet("commitDistance " + semver.git.commitDistance) // 28
logger.quiet("status " + semver.git.status)                 // dirty
```

The plugin exposes a `Semver`. See [Semver4J](https://javadoc.io/doc/org.semver4j/semver4j/latest/index.html).

`Semver` may be subclassed to provide a more appropriate `toString()` method which you should use instead
of `getVersion()` to provide to gradle and maven. For example `toString()` replaces `+` with `-` for maven.

```kt
version = semver.maven
version = semver.gradlePlugin

version.major // e.g. 1
version.minor // e.g. 0
```

See [Semver4J](https://javadoc.io/doc/org.semver4j/semver4j/latest/index.html) for more methods.

### Warning

Prerelease sub versions are not yet implemented

e.g. if you tag `v0.1.1-rc.1` and then add a commit you will still get a version semantically equivalent
to `0.1.1-rc.1` as the version. This will be fixed in future versions, and will probably look something
like `0.1.1-rc.2.alpha.1`

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
