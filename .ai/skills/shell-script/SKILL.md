---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-SA-4.0

name: shell-script
description: Write a shell script
license: CC-BY-NC-SA-4.0
metadata:
  author: Caleb Cushing
allowed-tools: sh zsh dash bash sed awk grep find xargs cut tr sort uniq head tail wc curl wget
---

- always verify with `shellcheck` for best practices, and fix any issues
- only write a posix compliant shell script unless otherwise specified or in a shell-specific file such `.zshrc` or files with extensions like `.bash` or `.zsh`
