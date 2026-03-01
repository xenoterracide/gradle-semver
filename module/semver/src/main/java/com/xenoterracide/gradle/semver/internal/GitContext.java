// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.immutables.builder.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Immutable value object containing all git-derived context for version calculation.
 *
 * @param nearestTag the nearest semantic version tag (e.g., "v1.0.0"), or null if no tags exist
 * @param distanceFromTag number of commits from the nearest tag to HEAD
 * @param isOnTagExact true if HEAD is exactly on a tag (distanceFromTag == 0)
 * @param currentBranch the current branch name, or null if detached HEAD
 * @param headBranch the HEAD branch name from origin (e.g., "main", "develop"), or null if unknown
 * @param isHeadBranch true if current branch is the HEAD branch
 * @param distanceFromMergeBase number of commits from merge base to HEAD
 * @param shortSha the abbreviated commit SHA
 * @param fullSha the full 40-character commit SHA
 * @param isDirty true if working tree has uncommitted changes
 * @param isShallowClone true if repository is a shallow clone
 */
@Builder
public record GitContext(
  @Nullable String nearestTag,
  long distanceFromTag,
  boolean isOnTagExact,
  @Nullable String currentBranch,
  @Nullable String headBranch,
  boolean isHeadBranch,
  long distanceFromMergeBase,
  String shortSha,
  String fullSha,
  boolean isDirty,
  boolean isShallowClone
) {
  /**
   * Creates a builder for GitContext.
   *
   * @return a new builder instance
   */
  public static GitContextBuilder builder() {
    return new GitContextBuilder();
  }

  /**
   * Checks if any tags exist in the repository history.
   *
   * @return true if a nearest tag exists
   */
  public boolean hasTagInHistory() {
    return this.nearestTag != null;
  }

  /**
   * Checks if we're on a topic branch (not the HEAD branch and not detached).
   *
   * @return true if on a topic branch
   */
  public boolean isTopicBranch() {
    return !this.isHeadBranch && this.currentBranch != null;
  }

  /**
   * Gets the base version string without the 'v' prefix.
   *
   * @return the base version (e.g., "1.0.0"), or null if no tag
   */
  public @Nullable String baseVersion() {
    if (this.nearestTag == null) {
      return null;
    }
    return this.nearestTag.startsWith("v") ? this.nearestTag.substring(1) : this.nearestTag;
  }
}
