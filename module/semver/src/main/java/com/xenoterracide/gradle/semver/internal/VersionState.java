// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.semver4j.Semver;

/**
 * State in the version calculation state machine.
 * Each implementation represents a specific git state and calculates
 * the appropriate semantic version for that state.
 */
// CHECKSTYLE.OFF: LeftCurly - prettier formatting conflicts with checkstyle
public sealed interface VersionState
  permits
    OnExactTagHeadBranch,
    OnExactTagTopicBranch,
    AfterTagHeadBranch,
    AfterTagTopicBranch,
    NoTagHeadBranch,
    NoTagTopicBranch
{
  // CHECKSTYLE.ON: LeftCurly
  /**
   * Calculates the semantic version for this state.
   *
   * @param ctx the git context
   * @return the calculated semantic version
   */
  Semver calculate(GitContext ctx);

  /**
   * Sanitizes a branch name for use in version metadata.
   * Replaces non-alphanumeric characters with hyphens.
   *
   * @param branch the branch name
   * @return sanitized branch name
   */
  default String sanitizeBranchName(String branch) {
    return branch.replaceAll("[^a-zA-Z0-9]", "-");
  }
}
