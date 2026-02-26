// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.jspecify.annotations.Nullable;

/**
 * Immutable value object containing all git-derived context for version calculation.
 *
 * @param nearestTag          The nearest semantic version tag (e.g., "v1.0.0"), or null if no tags exist
 * @param distanceFromTag     Number of commits from the nearest tag to HEAD
 * @param isOnTagExact        True if HEAD is exactly on a tag (distanceFromTag == 0)
 * @param currentBranch       The current branch name, or null if detached HEAD
 * @param headBranch          The HEAD branch name from origin (e.g., "main", "develop"), or null if unknown
 * @param isHeadBranch        True if current branch is the HEAD branch
 * @param distanceFromMergeBase Number of commits from merge base to HEAD (0 if on merge base or HEAD branch)
 * @param shortSha            The abbreviated commit SHA
 * @param fullSha             The full 40-character commit SHA
 * @param isDirty             True if working tree has uncommitted changes
 * @param isShallowClone      True if repository is a shallow clone
 */
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
   * Checks if any tags exist in the repository history.
   *
   * @return true if a nearest tag exists
   */
  public boolean hasTagInHistory() {
    return nearestTag != null;
  }

  /**
   * Checks if we're on a topic branch (not the HEAD branch and not detached).
   *
   * @return true if on a topic branch
   */
  public boolean isTopicBranch() {
    return !isHeadBranch && currentBranch != null;
  }

  /**
   * Gets the base version string without the 'v' prefix.
   *
   * @return the base version (e.g., "1.0.0"), or null if no tag
   */
  public @Nullable String baseVersion() {
    if (nearestTag == null) {
      return null;
    }
    return nearestTag.startsWith("v") ? nearestTag.substring(1) : nearestTag;
  }

  @Override
  public String toString() {
    return String.format(
      "GitContext[tag=%s, distanceFromTag=%d, onTagExact=%s, branch=%s, headBranch=%s, isHeadBranch=%s, " +
      "distanceFromMergeBase=%d, sha=%s, dirty=%s]",
      nearestTag, distanceFromTag, isOnTagExact, currentBranch, headBranch, isHeadBranch,
      distanceFromMergeBase, shortSha, isDirty
    );
  }
}
