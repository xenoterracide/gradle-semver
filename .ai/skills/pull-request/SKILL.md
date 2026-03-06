---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-4.0

name: github
description: work on pull request
license: CC-BY-NC-4.0
metadata:
  author: Caleb Cushing
allowed-tools: Shell(gh:*) Shell(git:*)
---

- commits must be pushed
- required workflows must pass
- git push --force is not allowed
- must be synchronized with HEAD branch using a merge strategy
  - it is easier to delete and regenerate lockfiles than merge them
- use commit-or-pr-message
- keep the pull request message up to date
- reply to PR comments and fix if desirable
