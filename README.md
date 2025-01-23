<!--
SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-4.0
-->

# README

This repo hosts 2 plugins, [semver](module/semver/README.md) and [git](module/git/README.md).

This plugin expects that you will `git tag` in the format of `v0.1.1` and with only one number on prerelease versions,
e.g. `v0.1.1-rc.1`. It also expects that you will use annotated tags.

## FAQ

### Shallow Clones

```
shallow clone detected! git only has {} commits
```

Shallow clones will not work properly with calculating the distance and thus you must not use them. The usual reason for doing a shallow clone is that repositories can grow quiet large, and it can be quite slow to download a 100Mb repository. What most people don't realize is that git is lazy and will fetch blobs as it needs them for a checkout if you do things correctly. `git add remote <origin> <https://...>` and then doing `git fetch --all --filter blob:none` followed by an operation like `git checkout <branch>` will not retrieve any files until you do the git checkout but it will have your full history. This will achieve the correct behavior on github.

```yml
- uses: actions/checkout@v4
  with:
    ref: ${{ github.event.workflow_run.head_branch}}
    filter: "blob:none"
    fetch-depth: 0
```

### Annotated Tags

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

Copyright © 2024 - 2025 Caleb Cushing
