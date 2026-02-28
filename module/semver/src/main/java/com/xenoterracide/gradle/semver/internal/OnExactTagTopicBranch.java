// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

/**
 * State: HEAD is exactly on a tag, but we're on a topic branch.
 *
 * <p>This can happen if a topic branch is created but has no new commits
 * beyond the tag. The metadata indicates we're on a topic branch for
 * traceability.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>v1.0.0 on feature-x (0 new commits) → {@code 1.0.0+branch.feature-x.git.0.abc123}</li>
 * </ul>
 */
public final class OnExactTagTopicBranch implements VersionState {

  @Override
  public Semver calculate(GitContext ctx) {
    @Nullable
    String baseVersion = ctx.baseVersion();
    if (baseVersion == null) {
      throw new IllegalStateException("OnExactTagTopicBranch requires a tag but baseVersion is null");
    }

    var semver = Semver.parse(baseVersion);
    if (semver == null) {
      throw new IllegalStateException("Invalid tag format: " + ctx.nearestTag());
    }

    // Add metadata to indicate we're on a topic branch at the tag
    var branchName = ctx.currentBranch() != null ? ctx.currentBranch() : "unknown";
    String metadata = String.format("branch.%s.git.0.%s", sanitizeBranchName(branchName), ctx.shortSha());

    return semver.withBuild(metadata);
  }
}
