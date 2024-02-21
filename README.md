# README

_Plugin ID_: `"com.xenoterracide.gradle.semver"`
_Version_: `0.9.+`

## Usage

_IMPORTANT_: this plugin will not work if you haven't run `git init` in your project.

```kt
plugin {
  id("com.xenoterracide.gradle.semver") version "0.+"
}

version = semver.version
```

- If no commits, or version tags have been made your version will be `0.0.0-SNAPSHOT`.
- If no valid tags are detected then your version will be `0.0.0-SNAPSHOT`.
- If you've used a valid version tag matching the pattern `v\d+\.\d+\.\d+` then your version will be that tag.
- If you've made commits after a valid version tag then your version will be `v\d+\.\d+\.\d+-SNAPSHOT-\d+-g\\p{XDigit}{7}`.

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

Apache 2.0
