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
logger.quiet("maven snapshot" + semver.mavenSnapshot) // e.g. 0.1.0-SNAPSHOT-1-gb001c8c
logger.quiet("maven alpha" + semver.mavenAlpha) // e.g. 0.1.0-alpha.1001255204163142
logger.quiet("gradlePlugin" + semver.gradlePlugin) // e.g 0.1.0-1-g3aae11e
logger.quiet("branch" + semver.git.branch)
logger.quiet("commit" + semver.git.commit)
logger.quiet("commitShort" + semver.git.commitShort)
logger.quiet("latestTag" + semver.git.latestTag)
logger.quiet("describe" + semver.git.describe)
logger.quiet("commitDistance" + semver.git.commitDistance)
logger.quiet("status" + semver.git.status)
```

The plugin exposes a `Semver` from https://github.com/semver4j/semver4j and uses `Semver.coerce`.

If you want you can do things like this

```kt
version = semver.maven // almost same as  semver.maven.version, because semver.toString() is the same as  semver.getVersion()
version = semver.gradlePlugin // semver.gradlePlugin.version

version.major // e.g. 1
version.minor // e.g. 0
```

See [Semver4J](https://javadoc.io/doc/org.semver4j/semver4j/latest/index.html) for more methods.

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
