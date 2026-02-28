<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-4.0
-->

- skills are in `.ai/skills`, use relevant skills
- `./gradlew check` must pass if `*.kts`, `*.java`, or `checkstyle/*.xml` has changed
- `yarn ug` updates gradle dependencies
- `gh` can be used if authenticated and available to:
  - open PR's
  - check PR comments
  - check build status
- formatting and license application is handled via `lint-staged` as a commit hooks. The commands it uses are found in the relevant `.lintstagedrc.yml`
- gradle buildHealth task does not use JPMS, and can be incorrect if JPMS is having with its suggestions.
- include links to upstream bugs in comments when relevant
- packages under group `com.xenoterracide` can be changed by us in other repos instead of being worked around
