// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.jspecify.annotations.Nullable;

/**
 * Immutable value object containing all git-derived context for version calculation.
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

  @Override
  public String toString() {
    return String.format(
      "GitContext[tag=%s, distanceFromTag=%d, onTagExact=%s, branch=%s, headBranch=%s, isHeadBranch=%s, " +
        "distanceFromMergeBase=%d, sha=%s, dirty=%s]",
      this.nearestTag,
      this.distanceFromTag,
      this.isOnTagExact,
      this.currentBranch,
      this.headBranch,
      this.isHeadBranch,
      this.distanceFromMergeBase,
      this.shortSha,
      this.isDirty
    );
  }
}
