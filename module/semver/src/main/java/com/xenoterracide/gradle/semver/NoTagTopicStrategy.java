// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.MoreObjects;
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
  private final GitContext ctx;

  NoTagTopicStrategy(GitContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Semver calculate() {
    // Start from 0.0.0
    // Prerelease uses distance from tag (total commits, like HEAD branch)
    // Metadata uses distance from merge base (commits on topic branch only)
    var branchName = MoreObjects.firstNonNull(this.ctx.currentBranch(), UNKNOWN);
    var shortSha = MoreObjects.firstNonNull(this.ctx.shortSha(), UNKNOWN);
    var prerelease = String.format("alpha.0.%d", this.ctx.distanceFromTag());
    var baseMetadata = String.format(
      "branch.%s.git.%d.%s",
      sanitizeBranchName(branchName),
      this.ctx.distanceFromMergeBase(),
      shortSha
    );
    var metadata = appendDirtyMarker(baseMetadata, this.ctx);

    // Topic branch uses same base version as HEAD (0.0.1)
    return Semver.ZERO.withIncPatch().withClearedPreRelease().withPreRelease(prerelease).withBuild(metadata);
  }
}
