// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.MoreObjects;
import com.xenoterracide.tools.java.util.ObjectTools;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.semver4j.Semver;

/**
 * Strategy: HEAD is after a tag, on a topic branch (not the
 * <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">HEAD branch</a>).
 *
 * <p>Examples (v1.0.0 was 5 commits ago on main):</p>
 * <ul>
 *   <li>3 commits on feature-x branch (branched from main) →
 *       {@code 1.0.1-alpha.0.5+branch.feature-x.git.3.abc123}
 *       (prerelease: 5 total from tag, like HEAD branch;
 *       metadata: 3 commits since <a href="https://git-scm.com/docs/git-merge-base">merge base</a>)</li>
 *   <li>1 commit on feature-x after v0.1.1-rc.1 →
 *       {@code 0.1.1-rc.1.1+branch.feature-x.git.1.abc123}</li>
 * </ul>
 *
 * <p>The prerelease uses distance from tag (same as HEAD branch would),
 * while metadata uses distance from merge base (commits on topic branch only).</p>
 */
final class AfterTagTopicStrategy implements VersionStrategy {

  private static final String UNKNOWN = "unknown";

  private final GitContext ctx;

  AfterTagTopicStrategy(GitContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Semver calculate() {
    var baseSemver = this.parseBaseVersion();
    var metadata = this.buildMetadata();
    // Prerelease uses distance from tag (HEAD branch style), metadata uses distance from merge base
    return buildVersion(baseSemver, this.ctx.distanceFromTag(), metadata);
  }

  private Semver parseBaseVersion() {
    var baseVersion = ObjectTools.illegalStateNull(
      this.ctx.baseVersion(),
      "AfterTagTopicStrategy requires a tag but baseVersion is null"
    );
    var semver = ObjectTools.illegalStateNull(
      Semver.parse(baseVersion),
      "Invalid tag format: " + this.ctx.nearestTag()
    );
    return semver;
  }

  private String buildMetadata() {
    var branchName = MoreObjects.firstNonNull(this.ctx.currentBranch(), UNKNOWN);
    var shortSha = MoreObjects.firstNonNull(this.ctx.shortSha(), UNKNOWN);
    // Metadata shows distance from merge base (commits on topic branch only)
    var baseMetadata = String.format(
      "branch.%s.git.%d.%s",
      sanitizeBranchName(branchName),
      this.ctx.distanceFromMergeBase(),
      shortSha
    );
    return this.appendDirtyMarker(baseMetadata, this.ctx);
  }

  private static Semver buildVersion(Semver baseSemver, long distance, String metadata) {
    // Topic branch uses same base version as HEAD branch
    // For stable tags: increment patch (same as HEAD branch)
    // For prerelease tags: use base as-is (same as HEAD branch)
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
