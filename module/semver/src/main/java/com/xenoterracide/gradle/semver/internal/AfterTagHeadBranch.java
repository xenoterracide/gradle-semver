// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

/**
 * State: HEAD is after a tag (distance > 0), on the HEAD branch (main/develop).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>5 commits after v1.0.0 on develop → {@code 1.0.1-alpha.0.5}</li>
 *   <li>1 commit after v0.1.1-rc.1 → {@code 0.1.1-rc.1.1}</li>
 * </ul>
 *
 * <p>No build metadata is added on the HEAD branch to keep versions clean.</p>
 */
public final class AfterTagHeadBranch implements VersionState {

  @Override
  // CHECKSTYLE.OFF: MethodLength
  public Semver calculate(GitContext ctx) {
    @Nullable
    String baseVersion = ctx.baseVersion();
    if (baseVersion == null) {
      throw new IllegalStateException("AfterTagHeadBranch requires a tag but baseVersion is null");
    }

    Semver semver = Semver.parse(baseVersion);
    if (semver == null) {
      throw new IllegalStateException("Invalid tag format: " + ctx.nearestTag());
    }

    // On HEAD branch, distanceFromTag == distanceFromMergeBase
    long distance = ctx.distanceFromTag();

    if (semver.getPreRelease().isEmpty()) {
      // For stable tags (e.g., v1.0.0), increment patch and add alpha prerelease
      String prerelease = String.format("alpha.0.%d", distance);
      return semver.withIncPatch().withClearedPreRelease().withPreRelease(prerelease);
    } else {
      // For prerelease tags (e.g., v1.0.0-rc.1), append distance to existing prerelease
      String prerelease = Stream.concat(semver.getPreRelease().stream(), Stream.of(Long.toString(distance))).collect(
        Collectors.joining(".")
      );
      return semver.withClearedPreRelease().withPreRelease(prerelease);
    }
  }
  // CHECKSTYLE.ON: MethodLength
}
