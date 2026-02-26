<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-4.0
-->

# Commands

- `./gradlew check` must pass if `*.kts`, `*.java`, or `checkstyle/*.xml` has changed
- `yarn ug` updates gradle dependencies
- `gh` can be used if authenticated and available.
- formatting and license application is handled via `yarn lint-staged`

# Quality

- gradle buildHealth task does not use JPMS, and can be incorrect if JPMS is having with its suggestions.
- include links to upstream bugs in comments when relevant

# Additional

- packages under group `com.xenoterracide` can be changed by us.
