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

Use for writing Java code.

## Design Principles

Let your domain language define the responsibilities in your system. Build each unit—object, function, or module—around a single responsibility derived from that language. Encapsulate behavior so it’s polymorphic, letting the unit decide how to act rather than orchestrating externally. If you follow these principles, your code will naturally be composable, clear, and aligned with the domain.

- prefer immutability over mutability
  - prefer `final` for fields unless they need to be mutable.
  - prefer `record` classes for simple data carriers.
  - `var strings = List.of("foo");` over `var strings = new ArrayList<String>(); strings.add("foo");`
  - Error Prone [Var](https://errorprone.info/bugpattern/Var) rule is enforced to prevent mutable variables. If you need mutability, you must explicitly annotate with `@Var` and justify why mutability is necessary.
- prefer non nullability. Using [jspecify](https://jspecify.dev/docs/spec/) and [Nullaway](https://github.com/uber/NullAway/wiki) we enforce non nullability by default and explicitly annotate nullable types with `@Nullable`. This helps prevent null pointer exceptions and makes it clear when a value can be null. `Optional` is preferred when mapping or filtering would be clearer than procedural logic.

## Style

### var keyword

prefer `var` keyword to explicit local variable type declaration. using var reduces quantity of code but more importantly coupling as sometimes it means classes no longer have to be imported and thus class name changes do not impact client code in some cases.

note: `@Var` is unrelated to this.

Examples:

#### GOOD

- `var x = 1;`
- `var foo = "foo"`
- `var list = new ArrayList<Foo>();`

#### BAD

- `int x = 1;`
- `String foo = "foo";`
- `List<Foo> list = new ArrayList<>();`

#### COUNTER EXAMPLE GOOD

- `Supplier<Foo> fooSupplier = () -> new Foo();` // not using var is better than casting

#### COUNTER EXAMPLE BAD

- `var fooSupplier = (Supplier<Foo>) () -> new Foo();` // casting is bad and should be avoided.

#### COUNTER EXAMPLE BETTER

Relying on a static method or variable can often be better than either even though it's more verbose.

```java
static Supplier<Foo> fooSupplier() {
  return () -> new Foo();
}
```

### Imports

- use `import` statements unless it would result in conflicts.
- use `static import` for test helpers that maintain clarity such as `assertThat` for AssertJ and `given` or `mock` from Mockito. Avoid static imports if it makes code comprehension.

### Visibility

Avoid `private` except with fields. prefer the default "package protected" unless must be `public` or is useful for subclasses.

- this allows methods to be exposed for testing but not outside the package. This aligns with the Vertical Slice architecture, Test Driven Principles, conventions where tests live in the same package and can access package protected methods, as well as original Java Language design that made this the default visibility. `private` is only necessary to prevent access from other classes in the same package, which is uncommon and should be avoided.
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
