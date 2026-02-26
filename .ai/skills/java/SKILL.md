---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-4.0

name: java
description: Write Java
license: CC-BY-NC-4.0
metadata:
  author: Caleb Cushing
allowed-tools: ./gradlew
---

- prefer `var`, and RHS generics, unless a class cast would be required.
- prefer `import` over fully qualified class names inlined
  - do not use `*` imports
- avoid `private` except with fields. prefer the default "package protected" unless must be `public` or is useful for subclasses.
- prefer `final` for fields unless they need to be mutable
