---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-SA-4.0

name: pull-request
description: work on PR (pull request)
license: CC-BY-NC-SA-4.0
metadata:
  author: Caleb Cushing
allowed-tools: Shell(gh:*) Shell(git:*) Shell(./gradlew:*) pull_request_read add_issue_comment add_reply_to_pull_request_comment update_pull_request list_pull_requests create_pull_request
---

- use commit-or-pr-message
- keep the pull request message up to date
- files should be committed and pushed
  - ensure code compiles and tests pass before committing
    - run relevant, specific tests first for quick feedback
    - prefer `./gradlew compile` for compile-only checks
    - run `./gradlew test` for quick test logic verification
    - run `./gradlew checkstyle` for checkstyle verification
    - run full `./gradlew check` before finalizing or when changes affect multiple modules.
  - verify GitHub PR checks pass after pushing
    - use available tools to check workflow status
    - fix any failures before requesting review
    - if Github checks fail after pushing, fix before requesting review
- git push --force is not allowed
- must be synchronized with HEAD branch using a merge strategy
  - it is easier to delete and regenerate lockfiles than merge them
- respond to ALL pr comments.
  - fix and comment if valid, or explain why not if invalid, ask if uncertain. This helps humans understand current comment status.
