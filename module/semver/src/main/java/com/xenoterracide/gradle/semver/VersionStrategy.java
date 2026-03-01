// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import org.semver4j.Semver;

/**
 * Strategy for calculating semantic version based on git context.
 * Each implementation represents a specific calculation strategy
 * for a particular git scenario.
 */
// CHECKSTYLE.OFF: LeftCurly
public sealed interface VersionStrategy
  permits
    OnExactTagHeadStrategy,
    OnExactTagTopicStrategy,
    AfterTagHeadStrategy,
    AfterTagTopicStrategy,
    NoTagHeadStrategy,
    NoTagTopicStrategy
{
  // CHECKSTYLE.ON: LeftCurly
  /**
   * Calculates the semantic version for this strategy.
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
