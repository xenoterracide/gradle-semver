// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import org.semver4j.Semver;

/**
 * Strategy: HEAD is exactly on a tag, and we're on the HEAD branch (main/develop).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>v1.0.0 on main → {@code 1.0.0}</li>
 * </ul>
 *
 * <p>This is the cleanest strategy - no prerelease or metadata needed.</p>
 */
final class OnExactTagHeadStrategy implements VersionStrategy {

  @Override
  public Semver calculate(GitContext ctx) {
    var baseVersion = ctx.baseVersion();
    if (baseVersion == null) {
      throw new IllegalStateException("OnExactTagHeadStrategy requires a tag but baseVersion is null");
    }

    var semver = Semver.parse(baseVersion);
    if (semver == null) {
      throw new IllegalStateException("Invalid tag format: " + ctx.nearestTag());
    }

    // On exact tag on HEAD branch: pure version without any suffixes
    return semver;
  }
}
