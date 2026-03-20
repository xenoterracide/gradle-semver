---
name: pull-request
description: ALWAYS use this skill when ANY files are modified, created, or deleted - including fixing bugs, adding features, refactoring, debugging, updating configs/workflows, or investigating issues that result in code changes. Handles committing, pushing, and creating/updating pull requests through GitHub.
# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: CC-BY-NC-SA-4.0
---

- use commit-message
- keep the pull request message up to date
  - NOTE: The PR description becomes the commit message when the PR is squash-merged
  - Follow the commit-message format for PR descriptions since they become permanent commit history
  - DO NOT use checkboxes (`- [x]`) in PR descriptions - they render poorly in commit messages
  - Use plain bullet lists (`- item`) instead of GitHub task lists
- files should be committed and pushed
  - ensure code compiles and tests pass before committing
    - run relevant, specific tests first for quick feedback
    - prefer `./gradlew compile` for compile-only checks
    - run `./gradlew test` for quick test logic verification
    - run `./gradlew checkstyle` for checkstyle verification
    - run full `./gradlew check` before finalizing or when changes affect multiple modules.
  - ensure documentation is up to date
    - review changes to understand what documentation may need updates
    - update `README.md` if user-facing behavior changes
    - update `AGENTS.md` if build processes, tools, or agent workflows change
    - when renaming workflows or changing their interface, update both README and AGENTS.md
  - verify GitHub PR checks pass after pushing
    - use available tools to check workflow status
    - fix any failures before requesting review
    - if Github checks fail after pushing, fix before requesting review
- git push --force is not allowed
- must be synchronized with HEAD branch using a merge strategy
  - it is easier to delete and regenerate lockfiles than merge them
- respond to ALL pr comments.
  - ALWAYS leave a reply on each review comment to indicate status
  - If fixed: comment "Fixed" or "Done" with brief explanation
  - If not an issue: comment explaining why (e.g., "Not applicable because...", "Already resolved...")
  - If uncertain: ask for clarification
  - This helps humans know whether to resolve the comment
  - only address UNRESOLVED comments - check review thread resolution status using GraphQL
  - use GraphQL to get review threads with `isResolved` field

## Workflow

When committing and creating/updating a PR, follow this workflow:

1. **Check current branch status** - Run `git status` and `gh pr view --json number,url,headRefName,state` to determine:
   - What branch you're currently on
   - Whether a PR already exists for this branch
   - Whether the PR is OPEN, CLOSED, or MERGED

2. **Handle closed/merged PRs:**
   - If the current branch has a CLOSED or MERGED PR, delete the local branch:
     - `git checkout develop` (the default HEAD branch)
     - `git branch -D <old-branch-name>`
   - Then create a new branch off the updated HEAD for new work

3. **Pull latest changes before starting work:**
   - Run `git pull origin develop` to get the latest changes
   - This ensures you're working on the current state and not outdated code
   - This also ensures you don't address review comments that are already resolved

4. **If already on a feature branch with an existing OPEN PR:**
   - Do NOT create a new branch
   - Pull latest changes first
   - Commit changes to the current branch
   - Push to update the existing PR
   - Update PR description/title if needed using `gh pr edit`

5. **If on develop or no PR exists for current branch:**
   - Create a new feature branch (if not already on one)
   - Commit changes
   - Push and create a new PR

6. **Before finalizing:**
   - Review if documentation needs updates (README.md, AGENTS.md)
   - Ensure PR description accurately reflects all changes including doc updates

### Squash Merge Strategy

This repository uses **squash merge** for PRs. This means:

- **Branch history doesn't matter** - All commits get squashed into one on merge
- **Merge is preferred over rebase** - When updating a branch with changes from develop, use `git merge origin/develop` instead of `git rebase`
- **Don't create new branches for updates** - If a PR needs changes, commit to the same branch and push
- **Don't worry about messy commits** - The squash merge cleans everything up

**When to update a branch:**

- If develop has moved forward and you need those changes: `git merge origin/develop`
- If review feedback requires changes: commit and push to same branch
- Avoid force push - repository rules may block it, and it's unnecessary with squash merge

## Creating/Updating PRs

### PR Title Format

Follow conventional commit format for PR titles (they become the squash merge commit message):

```
<type>(<scope>): <summary>
```

- Use commit types from `git-conventional-commits.yaml` (feat, fix, docs, etc.)
- Keep title <= 72 characters
- Use specific scope when possible

### Creating a New PR

Always provide explicit title and body. Do NOT use `--fill` as it may use the branch name instead of a proper conventional commit message:

```bash
# Get the commit message for the title
TITLE=$(git log -1 --format="%s" HEAD)

# Create PR with proper title and body
gh pr create --title "$TITLE" --body "- Bullet point describing change 1
- Bullet point describing change 2"
```

### Updating an Existing PR

```bash
gh pr edit --title "$TITLE" --body "- Updated bullet points"
```

### PR Body Format

- Short summary paragraph (optional)
- Bullet points explaining main changes
- Each bullet describes one complete logical change
- Explain WHAT and WHY
- Wrap lines to <= 72 characters

## Handling Review Comments

When addressing review comments on a PR:

1. **Pull first** - Always pull the latest changes before starting
2. **Query unresolved comments** - Use GraphQL to get only unresolved review threads:
   ```bash
   gh api graphql -f query='
   query {
     repository(owner: "OWNER", name: "REPO") {
       pullRequest(number: N) {
         reviewThreads(first: 100) {
           nodes {
             id
             isResolved
             comments(first: 1) {
               nodes {
                 id
                 body
                 path
                 originalLine
               }
             }
           }
         }
       }
     }
   }' --jq '.data.repository.pullRequest.reviewThreads.nodes | map(select(.isResolved == false))'
   ```
3. **Process unresolved** - The jq filter already returns only threads where `isResolved: false`
4. **Reply to each comment** - After making changes, reply to each review comment:
   - Fixed: `Fixed in commit SHA`
   - Not an issue: `Not applicable: [reason]`
   - Question: `Question: [clarification needed]`
   - Use `gh pr comment <number> --reply-to <comment-id>` if available, or quote the comment
5. **Verify fixes** - Confirm changes address the current code state

## Important Note on Comment APIs

The GitHub REST API (`/repos/{owner}/{repo}/pulls/{pull_number}/comments`) does NOT expose the "resolved" state of review comments. The resolution state is only available via:

- GraphQL API (`reviewThreads.isResolved`)
- GitHub Web UI

Always use GraphQL to check which review threads are actually unresolved.

## AI Attribution

When creating commits for a PR, include AI attribution in commit messages using a Co-authored-by trailer:

```
Co-authored-by: <AI_NAME> <AI_NOREPLY_EMAIL>
```

Use your AI identity:

| AI Name | `AI_NAME`        | `AI_NOREPLY_EMAIL`           |
| ------- | ---------------- | ---------------------------- |
| Kimi    | `Kimi`           | `kimi@moonshot.localhost`    |
| Copilot | `GitHub Copilot` | `copilot@github.localhost`   |
| Claude  | `Claude`         | `claude@anthropic.localhost` |

Place the Co-authored-by trailer at the end of the commit message body, after the description.
