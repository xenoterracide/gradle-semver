<!--
SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-4.0
-->

# Git Plugin

_Plugin ID_: `"com.xenoterracide.gradle.git"`
_Plugin GAV_: `"com.xenoterracide.gradle:git:0.12.+"
_Version_: `0.12.+`

## Usage

```kt
logger.quiet("branch:" + git.branch.getOrNull())
logger.quiet("commit:" + git.commit.getOrNull())
logger.quiet("commitShort:" + git.uniqueShort.getOrNull())
logger.quiet("latestTag:" + git.tag.getOrNull())
logger.quiet("commitDistance:" + git.distance.getOrNull())
logger.quiet("status:" + git.status.getOrNull())
```

Plugin will return nulls if it doesn't have any commits, or nulls for tags.
