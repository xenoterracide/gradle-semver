// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.semver4j.Semver;

/**
 * Strategy: HEAD is after a tag, on a topic branch (not HEAD branch).
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
final class AfterTagTopicStrategy implements VersionStrategy {

  private static final String UNKNOWN = "unknown";

  @Override
  public Semver calculate(GitContext ctx) {
    var baseVersion = ctx.baseVersion();
    if (baseVersion == null) {
      throw new IllegalStateException("AfterTagTopicStrategy requires a tag but baseVersion is null");
    }

    var baseSemver = Semver.parse(baseVersion);
    if (baseSemver == null) {
      throw new IllegalStateException("Invalid tag format: " + ctx.nearestTag());
    }

    var distance = ctx.distanceFromMergeBase();
    var branchName = ctx.currentBranch() != null ? ctx.currentBranch() : UNKNOWN;
    var shortSha = ctx.shortSha() != null ? ctx.shortSha() : UNKNOWN;
    var baseMetadata = String.format(
      "branch.%s.git.%d.%s",
      sanitizeBranchName(Objects.requireNonNull(branchName, "branchName")),
      distance,
      Objects.requireNonNull(shortSha, "shortSha")
    );
    var metadata = appendDirtyMarker(baseMetadata, ctx);

    return buildVersion(baseSemver, distance, metadata);
  }

  private static Semver buildVersion(Semver baseSemver, long distance, String metadata) {
    // If the nearest tag is a stable release (no prerelease), increment the patch
    // so that topic-branch versions (e.g. 1.0.1-alpha.0.N) sort after the tag.
    var targetBase = baseSemver.getPreRelease().isEmpty() ? baseSemver.withIncPatch() : baseSemver;
    var prerelease = buildPrerelease(targetBase, distance);
    return targetBase.withClearedPreRelease().withPreRelease(prerelease).withBuild(metadata);
  }

  private static String buildPrerelease(Semver baseSemver, long distance) {
    if (baseSemver.getPreRelease().isEmpty()) {
      return String.format("alpha.0.%d", distance);
    }
    return Stream.concat(baseSemver.getPreRelease().stream(), Stream.of(Long.toString(distance))).collect(
      Collectors.joining(".")
    );
  }
}
