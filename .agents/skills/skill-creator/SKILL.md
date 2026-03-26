---
name: skill-creator
description: Creating and maintaining AI skills for this project. Use when creating new skills, updating existing skills, or troubleshooting skill formatting issues.
---

<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# Skill Creator

Guidance for creating and maintaining AI skills.

## File Structure

```
skill-name/
├── SKILL.md (required)
│   └── Markdown instructions
└── (optional resources)
```

## SKILL.md Format

**CRITICAL:** Skills are fussy with frontmatter. The `---` must be the very
first line in the file.

### Correct Structure

```markdown
---
name: skill-name
description: When to use this skill. Be specific about triggers.
---

<!--
SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing

SPDX-License-Identifier: CC-BY-NC-SA-4.0
-->

# Skill Title

Content here...
```

### Common Mistakes

❌ **WRONG:** Comment before frontmatter

```markdown
<!--
SPDX-FileCopyrightText: ...
-->

---

name: skill-name
...
```

❌ **WRONG:** Blank line before frontmatter

```markdown
---
name: skill-name
...
```

✅ **CORRECT:** Frontmatter first, then HTML comment for copyright

```markdown
---
name: skill-name
description: ...
---

<!--
SPDX-FileCopyrightText: ...
-->
```

## Copyright and Licensing

- **Year:** Use current year (e.g., `2026`) for new skills, not a range
- **Location:** Place in HTML comment after frontmatter
- **Tool:** Do NOT use `reuse annotate` - it doesn't work with skill files
- **Manual:** Add the SPDX comment block manually

## Code Style

- Run `yarn exec prettier --write SKILL.md` after editing
- Prettier handles Markdown formatting
- No additional linting tools required for skills

## Testing Skills

Skills are recognized by Kimi when:

1. File is named `SKILL.md`
2. Located in `.agents/skills/<skill-name>/`
3. Frontmatter is valid (starts with `---`)
4. Has both `name` and `description` fields

## Best Practices

1. **Keep it concise** - Skills share context window with everything else
2. **Clear description** - The description determines when skill triggers
3. **Specific triggers** - Describe exact scenarios for skill usage
4. **Progressive disclosure** - Put detailed info in references/, keep
   SKILL.md focused
