<!--
SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# README

This repo hosts 2 plugins: [semver](module/semver/README.md) and [git](module/git/README.md).

The plugins expect git tags in the format `v0.1.1` (annotated tags) and prerelease versions like `v0.1.1-rc.1`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, build instructions, and development workflow.

## Goals

Provide semantic versioning for Maven publishing with Gradle.

_Future_: Provide a way to determine what the next version should be using your project's ABI.

## FAQ

### Shallow Clones

Shallow clones will not work properly for calculating version distance. Instead of a shallow clone, use:

```bash
git fetch --all --filter blob:none
```

Or in GitHub Actions:

```yaml
- uses: actions/checkout@v4
  with:
    filter: "blob:none"
    fetch-depth: 0
```

### Annotated Tags

[GitHub does not checkout annotated tags properly](https://github.com/actions/checkout/issues/882). Use this workaround:

```yaml
- uses: actions/checkout@v4
  with:
    ref: ${{ github.ref }}
```

### Version Support

| Version    | Gradle | Java | License                            |
| ---------- | ------ | ---- | ---------------------------------- |
| <= v0.13.x | 8.x    | 11.x | Apache 2.0                         |
| >= v0.14.x | 9.x    | 17.x | GPLv3 with Classpath Exception 2.0 |

## License

- **Java**: [GPLv3](https://choosealicense.com/licenses/gpl-3.0/) with [Classpath Exception](https://spdx.org/licenses/Classpath-exception-2.0.html)
- **Gradle/Kotlin/Config**: [MIT](https://choosealicense.com/licenses/mit/)
- **Documentation**: [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

Copyright © 2024 - 2026 Caleb Cushing
