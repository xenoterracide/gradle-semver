---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-4.0
# SPDX-License-Identifier: CC-BY-NC-SA-4.0

name: java
license: CC-BY-NC-SA-4.0
description: Write Java
metadata:
  author: Caleb Cushing
allowed-tools: ./gradlew
---

## Instructions

Use for writing Java code.

- prefer `var` keyword to explicit local variable type declaration. Examples:
  - `var x = 1;`
  - `var foo = "foo"`
- prefer immutable data structures over mutable ones.
  - `var strings = List.of("foo");` over `var strings = new ArrayList<String>(); strings.add("foo");`
- avoid `private` except with fields. prefer the default "package protected" unless must be `public` or is useful for subclasses.
- prefer `final` for fields unless they need to be mutable
- prefer `record` classes for simple data carriers.
- prefer builder pattern over complex constructors with immutables library `@Builder` and a static factory. e.g.

```java
import org.immutables.builder.Builder;

@Builder
record Bar(String foo) {
  public static BarBuilder builder() {
    return new BarBuilder();
  }
}
```
