HEAD := $(shell git rev-parse --verify HEAD)
GRADLE_DIR := $(wildcard ./.gradle/)
BUILD_DIRS := $(wildcard ./build/ */build/ ./module/*/build/)
CONFIGURATION_CACHE := $(wildcard $(GRADLE_DIR)configuration-cache/)

check_defined = $(strip $(foreach 1, $1,$(call __check_defined,$1,$(strip $(value 2)))))
__check_defined = $(if $(value $1),, $(error Undefined $1$(if $2, ($2))))

define gh_head_run_url
	gh run list --workflow $(1) --commit $(HEAD) --json url --jq '.[0].["url"]'
endef

define gh_head_run_id
	gh run list --workflow $(1) --commit $(HEAD) --json databaseId --jq '.[0].["databaseId"]'
endef

.PHONY: build
build:
	./gradlew spotlessApply build

.PHONY: up
up:
	./gradlew dependencies --write-locks --quiet 2>&1 > /dev/null

.PHONY: merge
merge: create-pr build watch-full merge-squash

.PHONY: clean
clean:
	./gradlew clean

.PHONY: cleaner
cleaner: clean-build clean-gradle

clean-cc: $(CONFIGURATION_CACHE)
	- rm -rf $(CONFIGURATION_CACHE)

ci-build:
	./gradlew build buildHealth --build-cache

ci-full:
	./gradlew buildHealth build --no-build-cache --no-configuration-cache

ci-update-java: clean-lockfiles up-wrapper up up-all-deps

clean-build:
	- rm -rf $(BUILD_DIRS)

clean-gradle:
	- rm -rf $(GRADLE_DIR)

clean-lockfiles:
	find . -name '*gradle.lockfile' -delete

up-wrapper:
	./gradlew wrapper --write-locks && ./gradlew wrapper

up-all-deps:
	./gradlew build buildHealth --write-locks --scan

create-pr:
	gh pr create || exit 0

merge-squash:
	gh pr merge --squash --delete-branch --auto

run-url:
	$(call check_defined, workflow)
	@$(call gh_head_run_url, $(workflow))

watch:
	$(call check_defined, workflow)
	@gh run watch $$($(call gh_head_run_id, $(workflow))) --exit-status

watch-full:
	@gh run watch $$($(call gh_head_run_id, "Full")) --exit-status
