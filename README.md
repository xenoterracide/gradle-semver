<!--
SPDX-License-Identifier: CC-BY-4.0
© Copyright 2024 Caleb Cushing. All rights reserved.
-->

# README

A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe.

_Plugin ID_: `"com.xenoterracide.gradle.semver"`
_Version_: `0.12.+`

## Usage

```kt
plugins {
  id("com.xenoterracide.gradle.semver")
}

version = semver.get()

```

This is the simplest way to get your semver, but I don't recommend it because gradle isn't lazy with anything related to publishing. Even when it becomes lazy I doubt it'll be as lazy as you want. So I do the following, and then only set `IS_PUBLISHING` in my publishing build in CI. This avoids constant configuration cache busting as well as ensuring that IO is kept to a minimum.

```kt
import org.semver4j.Semver

version = providers.environmentVariable("IS_PUBLISHING")
  .map { semver.get() }
  .orElse(Semver("0.0.0")).get()
```

This plugin expects that you will `git tag` in the format of `v0.1.1` and with only one number on prerelease versions,
e.g. `v0.1.1-rc.1`. It also expects that you will use annotated tags.

```kt
// given the last tag was v0.1.0 and you have a commit distance == 1 you'll get something like
logger.quiet("semver " + semver.get()        // 0.1.1-alpha.0.1+3aae11e

// other available outputs
logger.quiet("branch:" + gitMetadata.branch )
logger.quiet("commit:" + gitMetadata.commit)
logger.quiet("commitShort:" + gitMetadata.commitShort)
logger.quiet("latestTag:" + gitMetadata.latestTag)
logger.quiet("describe:" + gitMetadata.describe)
logger.quiet("commitDistance:" + gitMetadata.commitDistance)
logger.quiet("status:" + gitMetadata.status)
```

The plugin exposes a `Semver`. See [Semver4J](https://javadoc.io/doc/org.semver4j/semver4j/latest/index.html).

## Known Issues

- [GitHub does not checkout annotated tags properly](https://github.com/actions/checkout/issues/882)

You can use this snippet or another workaround documented on the issue

```yml
- uses: actions/checkout@v4
  with:
    ref: ${{ github.ref }}
```

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

#### Fetching Dependencies

In order to get snapshots of dependencies, you must have a GitHub token in your `~/.gradle/gradle.properties` file. This
file should look like:

```properties
ghUsername = <your username>
ghPassword = <your token>
```

You should generate your PAT
as [Github Documents here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages).

> a personal access token (classic) with at least read:packages scope to install packages associated with other private
> repositories (which GITHUB_TOKEN can't access).

Then run.

Run `npm ci && ./gradlew dependencies` to install dependencies.

### Committing

Use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

### Releasing

```sh
npm run release --semver="0.10.0"
```

## License

- Java: [Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)
- Gradle Kotlin and Config Files: [MIT](https://choosealicense.com/licenses/mit/)
- Documentation including Javadoc: [CC BY 4.0](https://choosealicense.com/licenses/cc-by-4.0/)

© Copyright 2024 Caleb Cushing.
