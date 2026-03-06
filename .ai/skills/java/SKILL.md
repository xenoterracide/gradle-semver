---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-SA-4.0

name: java
license: CC-BY-NC-SA-4.0
description: Write code in the Java programming language.
metadata:
  author: Caleb Cushing
allowed-tools: Shell(./gradlew:*)
---

## Design Principles

Let your domain language define the responsibilities in your system. Build each unit—object, function, or module—around a single responsibility derived from that language. Encapsulate behavior so it’s polymorphic, letting the unit decide how to act rather than orchestrating externally. If you follow these principles, your code will naturally be composable, clear, and aligned with the domain.

## Instructions

Use for writing Java code.

- prefer `var` keyword to explicit local variable type declaration. Examples:
  - `var x = 1;`
  - `var foo = "foo"`
- prefer immutable data structures over mutable ones.
  - `var strings = List.of("foo");` over `var strings = new ArrayList<String>(); strings.add("foo");`
- avoid `private` except with fields. prefer the default "package protected" unless must be `public` or is useful for subclasses.
  - this allows methods to be exposed for testing but not outside of the package.
- prefer `final` for fields unless they need to be mutable
- prefer `record` classes for simple data carriers.
- use `import` statements unless it would result in conflicts.
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
