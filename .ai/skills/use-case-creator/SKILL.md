---
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-SA-4.0

name: use-case-creator
license: CC-BY-NC-SA-4.0
metadata:
  author: Caleb Cushing
description: Create and maintain use case specifications following Cockburn format with semantic anchors and ubiquitous language. Use when writing use cases, specifying requirements, or documenting system behavior.
---

# Use Case Creator

Creates business-readable use cases that serve both human stakeholders and AI implementation.

## Output Format

Always create AsciiDoc (`.adoc`) files in `/docs/use-cases/`.

## Core Principles

### 1. Business-Readable Main Scenario

**DO:**

- Write steps a product owner can understand
- Use domain language (<<User>>, <<Account>>, <<Person>>)
- Describe business outcomes, not technical implementations

**DON'T:**

- Mention classes, methods, databases, or frameworks
- Use technical jargon ("aggregate", "repository", "handler")
- Describe code structure

### 2. Semantic Anchors for Stable References

Each step gets an anchor with the **full step text** as reftext:

```asciidoc
. [[step-validate,System validates identity is new]] *System validates identity is new* +
  _System queries whether <<IdentityProviderUser,identity>> already exists_
```

Extensions reference by anchor:

```asciidoc
=== During <<step-validate>>: User Already Exists
```

**Benefits:**

- Reordering steps doesn't break references
- Humans see full text in links
- AI uses stable `step-validate` identifier

### 3. Ubiquitous Language

Link domain terms to definitions:

- `<<User>>` - authentication entity
- `<<Person>>` - human being
- `<<Account>>` - resource/billing container
- `<<IdentityProviderUser>>` - external identity link

First create or reference the ubiquitous language in `/docs/ubiquitous-language/`.

## Template Structure

```asciidoc
= UC-XXX: {Name}

== Metadata
| Primary Actor | <<Person>> with {credentials}
| Scope | {Bounded Context}
| Level | User Goal

== Stakeholders and Interests
*{Stakeholder}*:: {What they want}

== Preconditions
* <<Person>> has {prerequisite}

== Postconditions
=== Success Guarantee
* <<User>> created
* <<DomainEvent>> published

== Main Success Scenario
. [[step-initiate,{Actor} initiates {action}]] *{Actor} initiates {action}* +
  _{Business result}_

. [[step-validate,System validates {condition}]] *System validates {condition}* +
  _{Validation logic}_

. [[step-create,System creates {entity}]] *System creates {entity}* +
  _{Creation outcome}_

== Extensions (Alternative Flows)

=== During <<step-validate>>: {Condition}
*Condition:* {When this happens}
*Steps:*
. {Alternative action}
. *Resume:* Continue at <<step-create>>

=== During <<step-validate>>: {Failure}
*Condition:* {Failure condition}
*Steps:*
. {Failure handling}
. *ENDS* (failed)

== Technology and Data Variations
* {Technical options}

== Special Requirements
* {Business constraints}

== Open Issues
* [[issue-xxx]] {Question to resolve}

== Related Use Cases
* xref:uc-xxx.adoc[UC-XXX: Name]
```

## Step Reference Patterns

| Pattern                       | Use When                  | Example                                    |
| ----------------------------- | ------------------------- | ------------------------------------------ |
| **During <<step-anchor>>**    | Extension interrupts step | `During <<step-validate>>: Invalid Input`  |
| **After <<step-anchor>>**     | Extension follows step    | `After <<step-create>>: Send Notification` |
| **Resume at <<step-anchor>>** | Return to main flow       | `Resume at <<step-complete>>`              |
| **ENDS**                      | Flow terminates           | `*ENDS* (success)` or `*ENDS* (failed)`    |

## File Naming

- `uc-{kebab-case}.adoc` for use cases
- `uc-{name}-contract.adoc` for implementation contracts (if needed)

## Example: Registration Use Case

See `assets/uc-registration-example.adoc` for a complete example.

## Workflow

1. **Identify actors** using ubiquitous language
2. **Define goal** - what success looks like
3. **Draft main scenario** - happy path only, business language
4. **Add extensions** - failure cases and variations
5. **Link domain terms** - add `<<Term>>` references
6. **Add metadata** - scope, level, trigger
7. **Record open issues** - unresolved questions
