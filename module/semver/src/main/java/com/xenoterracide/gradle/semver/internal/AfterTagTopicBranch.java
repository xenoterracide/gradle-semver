// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

/**
 * State: HEAD is after a tag, on a topic branch (not HEAD branch).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>3 commits on feature-x, which branched 5 commits after v1.0.0 →
 *       {@code 1.0.1-alpha.0.3+branch.feature-x.git.3.abc123}</li>
 *   <li>On feature-x with 1 commit after v0.1.1-rc.1 →
 *       {@code 0.1.1-rc.1.1+branch.feature-x.git.1.abc123}</li>
 * </ul>
 *
 * <p>The prerelease uses distance from merge base (commits on topic branch only),
 * and metadata includes branch name and the same distance.</p>
 */
public final class AfterTagTopicBranch implements VersionState {

  @Override
  public Semver calculate(GitContext ctx) {
    @Nullable
    String baseVersion = ctx.baseVersion();
    if (baseVersion == null) {
      throw new IllegalStateException("AfterTagTopicBranch requires a tag but baseVersion is null");
    }

    Semver baseSemver = Semver.parse(baseVersion);
    if (baseSemver == null) {
      throw new IllegalStateException("Invalid tag format: " + ctx.nearestTag());
    }

    // For prerelease: use distance from merge base (commits on topic branch only)
    // For metadata: include branch name and same distance
    long distance = ctx.distanceFromMergeBase();
    String branchName = ctx.currentBranch() != null ? ctx.currentBranch() : "unknown";

    String prerelease;
    Semver result;
    if (baseSemver.getPreRelease().isEmpty()) {
      // For stable tags, increment patch and add alpha prerelease
      prerelease = String.format("alpha.0.%d", distance);
      result = baseSemver.withIncPatch();
    } else {
      // For prerelease tags, append distance to existing prerelease
      prerelease = Stream.concat(baseSemver.getPreRelease().stream(), Stream.of(Long.toString(distance))).collect(
        Collectors.joining(".")
      );
      result = baseSemver;
    }

    String metadata = String.format("branch.%s.git.%d.%s", sanitizeBranchName(branchName), distance, ctx.shortSha());

    return result.withClearedPreRelease().withPreRelease(prerelease).withBuild(metadata);
  }
}
