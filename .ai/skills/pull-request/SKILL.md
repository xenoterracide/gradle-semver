---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-SA-4.0

name: pull-request
description: work on PR (pull request)
license: CC-BY-NC-SA-4.0
metadata:
  author: Caleb Cushing
allowed-tools: Shell(gh:*) Shell(git:*)
---

- use commit-or-pr-message
- keep the pull request message up to date
- files should be committed and pushed
  - ensure code compiles and tests pass before committing
    - prefer `./gradlew compile test` for quick feedback
    - run full `./gradlew check` when changes are substantial
    - always verify GitHub PR checks pass after pushing
- git push --force is not allowed
- must be synchronized with HEAD branch using a merge strategy
  - it is easier to delete and regenerate lockfiles than merge them
- reply to PR comments and fix if desirable
