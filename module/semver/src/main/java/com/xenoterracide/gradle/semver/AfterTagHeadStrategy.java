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
 * Strategy: HEAD is after a tag (distance > 0), on the
 * <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">HEAD branch</a>
 * (e.g., main, develop).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>5 commits after v1.0.0 on develop → {@code 1.0.1-alpha.0.5+git.5.abc123}</li>
 *   <li>1 commit after v0.1.1-rc.1 → {@code 0.1.1-rc.1.1+git.1.abc123}</li>
 * </ul>
 *
 * <p>Build metadata includes git distance and short SHA for traceability.</p>
 */
final class AfterTagHeadStrategy implements VersionStrategy {

  private static final String UNKNOWN = "unknown";

  private final GitContext ctx;

  AfterTagHeadStrategy(GitContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Semver calculate() {
    var baseVersion = ObjectTools.illegalStateNull(
      this.ctx.baseVersion(),
      "AfterTagHeadStrategy requires a tag but baseVersion is null"
    );

    var semver = ObjectTools.illegalStateNull(
      Semver.parse(baseVersion),
      "Invalid tag format: " + this.ctx.nearestTag()
    );

    var distance = this.ctx.distanceFromTag();
    var shortSha = MoreObjects.firstNonNull(this.ctx.shortSha(), UNKNOWN);
    var metadata = String.format("git.%d.%s", distance, shortSha);
    var metadataWithDirty = appendDirtyMarker(metadata, this.ctx);

    return calculateVersion(semver, distance).withBuild(metadataWithDirty);
  }

  private static Semver calculateVersion(Semver semver, long distance) {
    if (semver.getPreRelease().isEmpty()) {
      return calculateStableVersion(semver, distance);
    }
    return calculatePrereleaseVersion(semver, distance);
  }

  private static Semver calculateStableVersion(Semver semver, long distance) {
    var prerelease = String.format("alpha.0.%d", distance);
    return semver.withIncPatch().withClearedPreRelease().withPreRelease(prerelease);
  }

  private static Semver calculatePrereleaseVersion(Semver semver, long distance) {
    var prerelease = Stream.concat(semver.getPreRelease().stream(), Stream.of(Long.toString(distance))).collect(
      Collectors.joining(".")
    );
    return semver.withClearedPreRelease().withPreRelease(prerelease);
  }
}
