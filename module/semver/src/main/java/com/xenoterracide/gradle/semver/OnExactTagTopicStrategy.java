// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.MoreObjects;
import com.xenoterracide.tools.java.util.ObjectTools;
import org.semver4j.Semver;

/**
 * Strategy: HEAD is exactly on a tag, but we're on a topic branch (not the
 * <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">HEAD branch</a>).
 *
 * <p>This can happen if a topic branch is created but has no new commits
 * beyond the tag. The build metadata indicates we're on a topic branch for
 * traceability.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>v1.0.0 on feature-x (0 new commits) → {@code 1.0.0+branch.feature-x.git.0.abc123}</li>
 * </ul>
 */
final class OnExactTagTopicStrategy implements VersionStrategy {

  private static final String UNKNOWN = "unknown";
  private final GitContext ctx;

  OnExactTagTopicStrategy(GitContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Semver calculate() {
    var baseVersion = ObjectTools.illegalStateNull(
      this.ctx.baseVersion(),
      "OnExactTagTopicStrategy requires a tag but baseVersion is null"
    );

    var semver = ObjectTools.illegalStateNull(
      Semver.parse(baseVersion),
      "Invalid tag format: " + this.ctx.nearestTag()
    );

    // Add metadata to indicate we're on a topic branch at the tag
    var branchName = MoreObjects.firstNonNull(this.ctx.currentBranch(), UNKNOWN);
    var shortSha = MoreObjects.firstNonNull(this.ctx.shortSha(), UNKNOWN);
    var baseMetadata = String.format("branch.%s.git.0.%s", sanitizeBranchName(branchName), shortSha);
    var metadata = appendDirtyMarker(baseMetadata, this.ctx);

    return semver.withBuild(metadata);
  }
}
