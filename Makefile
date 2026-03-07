# SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
#
# SPDX-License-Identifier: MIT

HEAD = $(shell git rev-parse --verify HEAD)
ENGINE ?= junie
SKILL_FILE := .agents/skills/commit-or-pr-message/SKILL.md

# kimi uses --skills-dir for auto-discovery; other engines need --skill-file
ifeq ($(ENGINE),kimi)
  SKILL_ARG :=
else
  SKILL_ARG := --skill-file "$(SKILL_FILE)"
endif

define gh_head_run_id
	gh run list --workflow $(1) --commit $(HEAD) --json databaseId --jq '.[0].databaseId // ""'
endef

.PHONY: merge
merge: merge-head push
	@if gh pr view --json number > /dev/null 2>&1; then \
		$(MAKE) watch-build create-pr; \
	else \
		$(MAKE) create-pr watch-build; \
	fi
	@$(MAKE) merge-squash

create-pr:
	@tmp_dir=$$(mktemp -d); \
	head_before=$$(git rev-parse HEAD); \
	if gh pr view --json number > /dev/null 2>&1; then \
		printf '%s\n' "Updating PR message..."; \
		./.share/bin/pr-message.sh --engine "$(ENGINE)" --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
		  $(SKILL_ARG) || exit 1; \
		head_after=$$(git rev-parse HEAD); \
		if [ "$$head_before" != "$$head_after" ]; then \
			./.share/bin/pr-message.sh --engine "$(ENGINE)" --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
			  $(SKILL_ARG) || exit 1; \
		fi; \
		title=$$(cat "$$tmp_dir/title.txt"); \
		gh pr edit --title "$$title" --body-file "$$tmp_dir/body.txt" || exit 0; \
		GH_PAGER=cat gh pr view; \
	else \
		./.share/bin/pr-message.sh --engine "$(ENGINE)" --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
		  $(SKILL_ARG) || exit 1; \
		head_after=$$(git rev-parse HEAD); \
		if [ "$$head_before" != "$$head_after" ]; then \
			./.share/bin/pr-message.sh --engine "$(ENGINE)" --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
			  $(SKILL_ARG) || exit 1; \
		fi; \
		title=$$(cat "$$tmp_dir/title.txt"); \
		gh pr create --title "$$title" --body-file "$$tmp_dir/body.txt" || exit 0; \
		printf '%s\n' "PR created with generated message."; \
		GH_PAGER=cat gh pr view; \
	fi; \
	rm -rf "$$tmp_dir"

push:
	git push

merge-head:
	git fetch --all --prune --prune-tags --tags --force
	git merge origin/HEAD

merge-squash:
	@if [ -n "$$({ git status --porcelain=1 2>/dev/null; } )" ]; then \
		printf '%s\n' "WARNING: Uncommitted changes detected. Review before merge." 1>&2; \
	fi; \
	printf '%s' "Proceed with squash merge? [Y/n] "; \
	read -r reply < /dev/tty; \
	case "$$reply" in \
		[Nn]|[Nn][Oo]) printf '%s\n' "Merge cancelled."; exit 1 ;; \
		*) ;; \
	esac; \
	gh pr merge --squash --delete-branch

.PHONY: watch-build
watch-build:
	@printf "Waiting for workflow 'build' to start on commit $(HEAD)...\n"
	@run_id=""; \
	for i in $$(seq 1 12); do \
		run_id=$$($(call gh_head_run_id, "build")); \
		if [ -n "$$run_id" ]; then break; fi; \
		printf "Run not found yet, retrying in 5s... ($$i/12)\n"; \
		sleep 5; \
	done; \
	if [ -z "$$run_id" ]; then \
		printf "Error: Workflow 'build' did not start within 60 seconds.\n" >&2; \
		exit 1; \
	fi; \
	gh run watch "$$run_id" --exit-status
