// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.semver4j.Semver;

/**
 * Strategy: HEAD is after a tag (distance > 0), on the HEAD branch (main/develop).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>5 commits after v1.0.0 on develop → {@code 1.0.1-alpha.0.5}</li>
 *   <li>1 commit after v0.1.1-rc.1 → {@code 0.1.1-rc.1.1}</li>
 * </ul>
 *
 * <p>No build metadata is added on the HEAD branch to keep versions clean.</p>
 */
final class AfterTagHeadStrategy implements VersionStrategy {

  @Override
  public Semver calculate(GitContext ctx) {
    var baseVersion = ctx.baseVersion();
    if (baseVersion == null) {
      throw new IllegalStateException("AfterTagHeadStrategy requires a tag but baseVersion is null");
    }

    var semver = Semver.parse(baseVersion);
    if (semver == null) {
      throw new IllegalStateException("Invalid tag format: " + ctx.nearestTag());
    }

    return calculateVersion(semver, ctx.distanceFromTag());
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
