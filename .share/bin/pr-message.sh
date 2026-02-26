#!/bin/sh

# SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
#
# SPDX-License-Identifier: MIT

set -eu

usage() {
  cat <<'EOF'
Usage: pr-message.sh --title-file PATH --body-file PATH [--engine ENGINE] [--base-ref REF] [--skill-file PATH] [--dry-run]

Generates a Conventional Commit subject and body for PR title/body.
EOF
}

log() {
  [ -n "${COPILOT_PRMSG_DEBUG:-${COPILOT_COMMITMSG_DEBUG:-}}" ] && printf '%s\n' "$*" 1>&2
  return 0
}

YARN_CMD="yarn"
if ! command -v "$YARN_CMD" > /dev/null 2>&1; then
  if command -v corepack > /dev/null 2>&1; then
    YARN_CMD="corepack yarn"
  else
    printf '%s\n' "pr-message: ERROR: yarn not found (install or enable corepack)" 1>&2
    exit 1
  fi
fi

ENGINE="${PRMSG_ENGINE:-}"
TITLE_FILE=""
BODY_FILE=""
BASE_REF="${PRMSG_BASE_REF:-}"
SKILL_FILE="${PRMSG_SKILL_FILE:-}"
DRY_RUN=0

# Auto-detect available engines
is_engine_available() {
  case "$1" in
    copilot) command -v copilot > /dev/null 2>&1 ;;
    junie) command -v junie > /dev/null 2>&1 && command -v jq > /dev/null 2>&1 ;;
    kimi) command -v kimi > /dev/null 2>&1 ;;
    *) return 1 ;;
  esac
}

# If engine not specified, auto-detect
if [ -z "$ENGINE" ]; then
  for try_engine in junie kimi copilot; do
    if is_engine_available "$try_engine"; then
      ENGINE="$try_engine"
      break
    fi
  done
fi

# If still no engine found, error out
if [ -z "$ENGINE" ]; then
  printf '%s\n' "pr-message: ERROR: no AI engine found (tried: junie, kimi, copilot)" 1>&2
  exit 1
fi

while [ $# -gt 0 ]; do
  case "$1" in
    --engine)
      ENGINE="$2"
      shift 2
      ;;
    --title-file)
      TITLE_FILE="$2"
      shift 2
      ;;
    --body-file)
      BODY_FILE="$2"
      shift 2
      ;;
    --base-ref)
      BASE_REF="$2"
      shift 2
      ;;
    --skill-file)
      SKILL_FILE="$2"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=1
      shift 1
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      printf '%s\n' "Unknown argument: $1" 1>&2
      usage
      exit 2
      ;;
  esac
done

# Validate engine and fall back if not available
if ! is_engine_available "$ENGINE"; then
  log "pr-message: engine '$ENGINE' not available, trying fallback"
  ORIG_ENGINE="$ENGINE"
  FOUND=0
  for try_engine in junie kimi copilot; do
    if [ "$try_engine" != "$ORIG_ENGINE" ] && is_engine_available "$try_engine"; then
      ENGINE="$try_engine"
      FOUND=1
      log "pr-message: fell back to '$ENGINE'"
      break
    fi
  done
  if [ "$FOUND" -eq 0 ]; then
    printf '%s\n' "pr-message: ERROR: engine '$ORIG_ENGINE' not available and no fallback found" 1>&2
    exit 1
  fi
fi

log "pr-message: using engine '$ENGINE'"

if [ -z "$TITLE_FILE" ] || [ -z "$BODY_FILE" ]; then
  usage
  exit 2
fi

if [ -z "$BASE_REF" ]; then
  ORIGIN_HEAD_BRANCH=$(git remote show origin 2> /dev/null | awk '/HEAD branch/ {print $NF}')
  if [ -n "$ORIGIN_HEAD_BRANCH" ]; then
    BASE_REF="origin/$ORIGIN_HEAD_BRANCH"
  else
    BASE_REF="origin/HEAD"
  fi
fi

if ! git rev-parse --verify "$BASE_REF" > /dev/null 2>&1; then
  log "pr-message: base ref '$BASE_REF' not found; falling back to HEAD~1"
  BASE_REF="HEAD~1"
fi

DIFF_RANGE="$BASE_REF...HEAD"

if git diff --quiet --exit-code "$DIFF_RANGE"; then
  log "pr-message: no changes in $DIFF_RANGE"
  exit 2
fi

MAX_FILES="${PRMSG_MAX_FILES:-400}"
MAX_DIFF_LINES="${PRMSG_MAX_DIFF_LINES:-2000}"
MAX_BODY_LINES="${PRMSG_MAX_BODY_LINES:-${COPILOT_COMMITMSG_MAX_BODY_LINES:-12}}"
INCLUDE_CONVENTION_CONFIG="${PRMSG_INCLUDE_CONVENTION_CONFIG:-${COPILOT_COMMITMSG_INCLUDE_CONVENTION_CONFIG:-1}}"
REQUIRE_CONVENTION_CONFIG="${PRMSG_REQUIRE_CONVENTION_CONFIG:-${COPILOT_COMMITMSG_REQUIRE_CONVENTION_CONFIG:-0}}"
FAIL_ON_INVALID="${PRMSG_FAIL_ON_INVALID:-${COPILOT_COMMITMSG_FAIL_ON_INVALID:-1}}"

CHANGED_FILES=$(git diff --name-only "$DIFF_RANGE" | head -n "$MAX_FILES")
CHANGED_DIFF=$(git diff "$DIFF_RANGE" | head -n "$MAX_DIFF_LINES")

ALLOWED_TYPES="ci feat fix perf refactor style test build ops docs chore merge revert"
ALLOWED_TYPES_ALT=$(printf '%s' "$ALLOWED_TYPES" | tr ' ' '|')

CONVENTION_CONFIG_SNIPPET=""
if [ -f "git-conventional-commits.yaml" ]; then
  if [ "$INCLUDE_CONVENTION_CONFIG" != "0" ]; then
    CONVENTION_CONFIG_SNIPPET=$(sed -n '1,200p' git-conventional-commits.yaml | head -c 8192)
  fi
else
  if [ "$REQUIRE_CONVENTION_CONFIG" = "1" ]; then
    printf '%s\n' "pr-message: ERROR: git-conventional-commits.yaml is required but missing" 1>&2
    exit 1
  fi
fi

SKILL_SNIPPET=""
if [ -n "$SKILL_FILE" ]; then
  SKILL_SNIPPET=$(sed -n '1,200p' "$SKILL_FILE" | head -c 8192)
fi

PROMPT="You are writing a git commit message for a human developer.

You MUST follow this exact template:

<type>(<scope>): <summary>

<body>

Rules:
- Output plain text only. No markdown fences.
- First line MUST be a valid Conventional Commit subject.
- Keep the FIRST line <= 72 characters.
- Use a specific scope when possible.
- Body:
  - Provide 0-6 bullet points.
  - Explain WHAT changed and WHY.
  - Wrap body lines to <= 72 characters.
  - Do not repeat the subject.

IMPORTANT: Use ONLY these commit types: $ALLOWED_TYPES
If unsure, choose 'chore'.
"

if [ -n "$SKILL_SNIPPET" ]; then
  PROMPT="$PROMPT
Commit/PR message skill guidance:
---
$SKILL_SNIPPET
---
"
fi

if [ -n "$CONVENTION_CONFIG_SNIPPET" ]; then
  PROMPT="$PROMPT
Project commit convention (git-conventional-commits.yaml):
---
$CONVENTION_CONFIG_SNIPPET
---
"
fi

PROMPT="$PROMPT
Changed files (PR diff):
${CHANGED_FILES}

PR diff (truncated):
${CHANGED_DIFF}
"

log "pr-message: invoking $ENGINE (files: $(printf '%s\n' "$CHANGED_FILES" | wc -l | tr -d ' '))"

if [ "$ENGINE" = "junie" ]; then
  # Use provided skill or default to the one in the project
  SKILL_PROMPT=""
  [ -n "$SKILL_SNIPPET" ] && SKILL_PROMPT=" using this skill guidance: $SKILL_SNIPPET"

  # We ask Junie to write directly to the files to be faster and avoid manual parsing if possible.
  # We also include the diff to avoid Junie having to discover it.
  # Note: --cache-dir is set to .junie/cache as requested.
  # We don't use --output-format=json here so the user can see progress in the terminal.
  junie --skip-update-check --cache-dir=.junie/cache \
    "Generate a conventional commit message for the following diff and write the subject line to '$TITLE_FILE' and the body to '$BODY_FILE'. Do not run any tests or gradle commands.$SKILL_PROMPT

Diff:
$CHANGED_DIFF" || true

  # Fallback: if files are empty, try the old way or check if Junie wrote something to stdout (not likely with > /dev/null)
  # Actually, if Junie failed to write the files, we might want to try one more time or just fail.
  if [ ! -s "$TITLE_FILE" ]; then
    log "pr-message: junie didn't write to $TITLE_FILE, trying to capture output"
    AI_OUT=$(junie --skip-update-check --cache-dir=.junie/cache --output-format=json \
      "Generate a conventional commit message for the following diff and output it as plain text.$SKILL_PROMPT

Diff:
$CHANGED_DIFF" 2>&1 | jq -r ".result" || echo "")
  else
    # If files were written, we are done for Junie
    AI_OUT=""
  fi
elif [ "$ENGINE" = "kimi" ]; then
  # Use .ai/skills as base skills dir if it exists
  # Kimi auto-discovers skills from this directory and injects them into the
  # system prompt. The AI decides when to use the commit-or-pr-message skill.
  # No need to manually embed SKILL_SNIPPET - it would duplicate the content.
  SKILLS_DIR_ARG=""
  if [ -d ".ai/skills" ]; then
    SKILLS_DIR_ARG=".ai/skills"
  fi

  # Kimi invocation
  # --no-thinking: faster, more direct output
  # --quiet: alias for --print --output-format text --final-message-only
  # --skills-dir: enables auto-discovery of commit-or-pr-message skill
  if [ -n "$SKILLS_DIR_ARG" ]; then
    kimi --skills-dir "$SKILLS_DIR_ARG" --no-thinking --quiet --prompt \
      "Generate a conventional commit message for the following diff and write the subject line to '$TITLE_FILE' and the body to '$BODY_FILE'. Do not run any tests or gradle commands.

Diff:
$CHANGED_DIFF" || true
  else
    kimi --no-thinking --quiet --prompt \
      "Generate a conventional commit message for the following diff and write the subject line to '$TITLE_FILE' and the body to '$BODY_FILE'. Do not run any tests or gradle commands.

Diff:
$CHANGED_DIFF" || true
  fi

  AI_OUT=""
  if [ ! -s "$TITLE_FILE" ]; then
    log "pr-message: kimi didn't write to $TITLE_FILE"
  fi
else
  COPILOT_MODEL="${COPILOT_PRMSG_MODEL:-${COPILOT_COMMITMSG_MODEL:-gpt-5.1-codex-mini}}"
  COPILOT_FALLBACK_MODEL="${COPILOT_PRMSG_FALLBACK_MODEL:-${COPILOT_COMMITMSG_FALLBACK_MODEL:-gpt-5.1-codex}}"

  log "pr-message: copilot model=$COPILOT_MODEL (fallback=$COPILOT_FALLBACK_MODEL)"

  run_copilot() {
    _model="$1"
    _out_file="$2"
    _err_file="$3"

    copilot --model "$_model" -s -p "$PROMPT" > "$_out_file" 2> "$_err_file" || return $?
  }

  TMP_OUT=$(mktemp)
  TMP_ERR=$(mktemp)

  run_copilot "$COPILOT_MODEL" "$TMP_OUT" "$TMP_ERR" || true
  AI_OUT=$(cat "$TMP_OUT")
  COPILOT_ERR=$(cat "$TMP_ERR")

  if printf '%s\n%s' "$AI_OUT" "$COPILOT_ERR" | grep -qi 'enable this model'; then
    log "pr-message: model '$COPILOT_MODEL' not enabled; falling back to '$COPILOT_FALLBACK_MODEL'"
    run_copilot "$COPILOT_FALLBACK_MODEL" "$TMP_OUT" "$TMP_ERR" || true
    AI_OUT=$(cat "$TMP_OUT")
    COPILOT_ERR=$(cat "$TMP_ERR")
  fi

  if [ -z "${AI_OUT:-}" ]; then
    if [ -n "${COPILOT_ERR:-}" ] && [ -n "${COPILOT_PRMSG_DEBUG:-${COPILOT_COMMITMSG_DEBUG:-}}" ]; then
      printf '%s\n' "copilot(pr-message): stderr: $COPILOT_ERR" 1>&2
    fi

    log "pr-message: empty output; retrying with fallback model '$COPILOT_FALLBACK_MODEL'"
    run_copilot "$COPILOT_FALLBACK_MODEL" "$TMP_OUT" "$TMP_ERR" || true
    AI_OUT=$(cat "$TMP_OUT")
    COPILOT_ERR=$(cat "$TMP_ERR")
  fi

  rm -f "$TMP_OUT" "$TMP_ERR"
fi

AI_OUT=$(printf '%s' "$AI_OUT" | sed -e 's/\r$//')

# If AI_OUT is empty but files exist, it means Junie/Kimi wrote them directly
if [ -z "$AI_OUT" ] && [ -s "$TITLE_FILE" ]; then
  SUBJECT=$(cat "$TITLE_FILE")
  # We still want to ensure it's trimmed and fits constraints
  SUBJECT=$(printf '%s' "$SUBJECT" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//' | cut -c1-72)
  printf '%s\n' "$SUBJECT" > "$TITLE_FILE"
  log "pr-message: $ENGINE wrote title/body directly"

  if [ "$DRY_RUN" -eq 1 ]; then
    printf '%s\n' "Title:"
    cat "$TITLE_FILE"
    printf '\n%s\n' "Body:"
    [ -s "$BODY_FILE" ] && cat "$BODY_FILE"
  fi
  exit 0
fi

SUBJECT_CANDIDATE=$(
  printf '%s\n' "$AI_OUT" \
    | sed -E 's/^Subject:[[:space:]]*//I' \
    | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//' \
    | awk -v types="$ALLOWED_TYPES_ALT" '
      BEGIN{
        re="^(" types ")(([(][^)]*[)]))?: .+";
      }
      {
        line=$0
        if (line ~ /^$/) next
        if (line ~ re) { print line; exit }
        if (ENVIRON["PRMSG_ECHO_IGNORED"] != "") {
          printf("%s\n", "ai(pr-message): ignored: " line) > "/dev/stderr"
        }
      }
    ' \
    | head -n 1
)

SUBJECT_CANDIDATE=$(printf '%s' "$SUBJECT_CANDIDATE" | sed -E "s/^(I'll|I will|Sure,?|Here's|Proposed|Suggested)[: -]+//I")

if [ -z "${SUBJECT_CANDIDATE:-}" ]; then
  if [ "${FAIL_ON_INVALID:-1}" = "1" ]; then
    log "pr-message: $ENGINE failed to yield a valid subject; using error fallback"
    SUBJECT="error($ENGINE): failed to generate message"

    {
      printf '%s failed to generate a conventional commit message.\n\n' "$ENGINE"
      if [ -n "${AI_OUT:-}" ]; then
        printf 'Output:\n%s\n\n' "$AI_OUT"
      fi
      if [ "$ENGINE" = "copilot" ] && [ -n "${COPILOT_ERR:-}" ]; then
        printf 'Errors:\n%s\n' "$COPILOT_ERR"
      fi
    } > "$BODY_FILE"
  else
    log "pr-message: $ENGINE did not yield a conventional subject; using heuristic fallback"
    if printf '%s\n' "$CHANGED_FILES" | grep -Eq '(^|/)(README\.md|.*\.md)$'; then
      SUBJECT_CANDIDATE="docs: update documentation"
    elif printf '%s\n' "$CHANGED_FILES" | grep -Eq '(^|/)\.github/'; then
      SUBJECT_CANDIDATE="ci: update workflows"
    elif printf '%s\n' "$CHANGED_FILES" | grep -Eq '\.gradle\.kts$|^gradle/|^settings\.gradle\.kts$|^build\.gradle\.kts$'; then
      SUBJECT_CANDIDATE="build: update build"
    else
      SUBJECT_CANDIDATE="chore: update"
    fi
    SUBJECT=$(printf '%s' "$SUBJECT_CANDIDATE" | cut -c1-72)
    printf '' > "$BODY_FILE"
  fi
else
  SUBJECT=$(printf '%s' "$SUBJECT_CANDIDATE" | cut -c1-72)

  BODY=$(printf '%s\n' "$AI_OUT" | awk 'BEGIN{blank=0} NR==1{next} /^$/{blank=1; next} blank{print}')

  if [ -z "${BODY:-}" ]; then
    BODY=$(printf '%s\n' "$AI_OUT" | sed -e '1d' | awk 'NF')
  fi

  BODY=$(printf '%s\n' "$BODY" | grep -viE "^(i'll|i will|sure|here's|proposed|suggested|i inspected|let's)\\b" || true)

  BODY=$(printf '%s\n' "$BODY" | awk -v max="$MAX_BODY_LINES" 'BEGIN{n=0} {print} NF{n++} (n>=max){exit}')

  if [ -n "${BODY:-}" ]; then
    printf '%s\n' "$BODY" | fold -s -w 72 > "$BODY_FILE"
  else
    printf '' > "$BODY_FILE"
  fi
fi

printf '%s\n' "$SUBJECT" > "$TITLE_FILE"

if [ "$DRY_RUN" -eq 1 ]; then
  printf '%s\n' "Title:"
  cat "$TITLE_FILE"
  printf '\n%s\n' "Body:"
  if [ -s "$BODY_FILE" ]; then
    cat "$BODY_FILE"
  fi
fi

log "pr-message: wrote title/body"
