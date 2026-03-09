// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import org.apache.commons.lang3.ObjectUtils;
import org.semver4j.Semver;

/**
 * Strategy: No tags exist in the repository, on a topic branch (not the
 * <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">HEAD branch</a>).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>New repo with 5 commits on main, feature-x branched
 *       with 2 new commits → {@code 0.0.1-alpha.0.5+branch.feature-x.git.2.abc123}
 *       (prerelease: 5 total commits, like HEAD branch;
 *       metadata: 2 commits since <a href="https://git-scm.com/docs/git-merge-base">merge base</a>)</li>
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
    var branchName = ObjectUtils.firstNonNull(this.ctx.currentBranch(), UNKNOWN);
    var shortSha = ObjectUtils.firstNonNull(this.ctx.shortSha(), UNKNOWN);
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
