# SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
#
# SPDX-License-Identifier: MIT

HEAD := $(shell git rev-parse --verify HEAD)
SKILL_FILE := .ai/skills/commit-or-pr-message/SKILL.md
PR_MSG := .repo/share/scripts/pr-message.sh
define gh_head_run_id
	gh run list --workflow $(1) --commit $(HEAD) --json databaseId --jq '.[0].["databaseId"]'
endef

.PHONY: build
build:
	./gradlew build --console=plain

.PHONY: merge
merge: merge-head push
	@if gh pr view --json number > /dev/null 2>&1; then \
		$(MAKE) watch-full create-pr; \
	else \
		$(MAKE) create-pr watch-full; \
	fi
	@$(MAKE) merge-squash

create-pr: build
	@tmp_dir=$$(mktemp -d); \
	head_before=$$(git rev-parse HEAD); \
	if gh pr view --json number > /dev/null 2>&1; then \
		printf '%s\n' "Updating PR message..."; \
		PR_MSG --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
		  --skill-file "$(SKILL_FILE)" || exit 0; \
		head_after=$$(git rev-parse HEAD); \
		if [ "$$head_before" != "$$head_after" ]; then \
			PR_MSG --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
			  --skill-file "$(SKILL_FILE)" || exit 0; \
		fi; \
		title=$$(cat "$$tmp_dir/title.txt"); \
		gh pr edit --title "$$title" --body-file "$$tmp_dir/body.txt" || exit 0; \
		GH_PAGER=cat gh pr view; \
	else \
		PR_MSG --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
		  --skill-file "$(SKILL_FILE)" || exit 0; \
		head_after=$$(git rev-parse HEAD); \
		if [ "$$head_before" != "$$head_after" ]; then \
			PR_MSG --title-file "$$tmp_dir/title.txt" --body-file "$$tmp_dir/body.txt" \
			  --skill-file "$(SKILL_FILE)" || exit 0; \
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
	read -r reply; \
	case "$$reply" in \
		""|y|Y|yes|YES) ;; \
		*) printf '%s\n' "Merge cancelled."; exit 1 ;; \
	esac; \
	gh pr merge --squash --delete-branch --auto

watch-full:
	@gh run watch $$($(call gh_head_run_id, "full")) --exit-status
