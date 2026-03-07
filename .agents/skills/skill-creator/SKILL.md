<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
SPDX-License-Identifier: CC0-1.0
-->

# Skill Creator

Guide for creating effective skills that extend Claude's capabilities.

## About Skills

Skills are modular, self-contained packages that extend Claude's capabilities by providing specialized knowledge, workflows, and tools. Think of them as "onboarding guides" for specific domains.

### What Skills Provide

1. **Specialized workflows** - Multi-step procedures for specific domains
2. **Tool integrations** - Instructions for working with specific file formats or APIs
3. **Domain expertise** - Company-specific knowledge, schemas, business logic
4. **Bundled resources** - Scripts, references, and assets for complex tasks

## Core Principles

### Concise is Key

The context window is a public good. Only add context Claude doesn't already have.

**Default assumption: Claude is already very smart.** Challenge each piece of information: "Does Claude really need this explanation?"

Prefer concise examples over verbose explanations.

### Anatomy of a Skill

```
skill-name/
├── SKILL.md (required)
│   ├── YAML frontmatter (name, description)
│   └── Markdown instructions
└── Bundled Resources (optional)
    ├── scripts/      - Executable code
    ├── references/   - Documentation
    └── assets/       - Templates, images
```

## SKILL.md Components

### Frontmatter (YAML)

```yaml
---
name: skill-name
description: What the skill does. Use when [activation trigger].
---
```

The description is the primary triggering mechanism. Include both what the skill does AND when to use it.

### Body (Markdown)

Instructions and guidance. Only loaded AFTER the skill triggers.

## Bundled Resources

### Scripts (`scripts/`)

Executable code for tasks requiring deterministic reliability.

### References (`references/`)

Documentation loaded as needed into context.

### Assets (`assets/`)

Files used in output (templates, images, fonts).

## Progressive Disclosure

Skills use three-level loading:

1. **Metadata** - Always in context (~100 words)
2. **SKILL.md body** - When skill triggers (<5k words)
3. **Bundled resources** - As needed

Keep SKILL.md under 500 lines. Split content when approaching this limit.

## Skill Creation Process

1. **Understand** - Gather concrete usage examples
2. **Plan** - Identify reusable scripts, references, assets
3. **Initialize** - Create skill directory structure
4. **Edit** - Implement resources and write SKILL.md
5. **Package** - Bundle for distribution
6. **Iterate** - Improve based on real usage

## What NOT to Include

- README.md
- INSTALLATION_GUIDE.md
- CHANGELOG.md
- User-facing documentation

Skills are for AI agents, not humans. Only include what Claude needs to do the job.
