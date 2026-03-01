// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import java.util.Objects;
import org.semver4j.Semver;

/**
 * Strategy: No tags exist in the repository, on a topic branch.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>New repo with 5 commits on main, feature-x branched
 *       with 2 new commits → {@code 0.0.1-alpha.0.2+branch.feature-x.git.2.abc123}</li>
 * </ul>
 */
final class NoTagTopicStrategy implements VersionStrategy {

  private static final String UNKNOWN = "unknown";

  @Override
  public Semver calculate(GitContext ctx) {
    // Start from 0.0.0
    // Prerelease uses distance from merge base (commits on topic branch)
    // Metadata includes branch name and distance
    var branchName = ctx.currentBranch() != null ? ctx.currentBranch() : UNKNOWN;
    var shortSha = ctx.shortSha() != null ? ctx.shortSha() : UNKNOWN;
    var prerelease = String.format("alpha.0.%d", ctx.distanceFromMergeBase());
    var metadata = String.format(
      "branch.%s.git.%d.%s",
      sanitizeBranchName(Objects.requireNonNull(branchName, "branchName")),
      ctx.distanceFromMergeBase(),
      Objects.requireNonNull(shortSha, "shortSha")
    );

    return Semver.ZERO.withIncPatch().withClearedPreRelease().withPreRelease(prerelease).withBuild(metadata);
  }
}
