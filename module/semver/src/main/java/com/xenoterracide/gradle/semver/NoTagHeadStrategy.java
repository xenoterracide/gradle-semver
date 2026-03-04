// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.MoreObjects;
import org.semver4j.Semver;

/**
 * Strategy: No tags exist in the repository, on the HEAD branch.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>New repo with 5 commits on main → {@code 0.0.1-alpha.0.5+git.5.abc123}</li>
 * </ul>
 *
 * <p>Starts from 0.0.0 and adds prerelease with total commits and metadata.</p>
 */
final class NoTagHeadStrategy implements VersionStrategy {

  private static final String UNKNOWN = "unknown";

  private final GitContext ctx;

  NoTagHeadStrategy(GitContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Semver calculate() {
    // Start from 0.0.0, add prerelease with total commits
    // On HEAD branch, distanceFromTag represents total commits (since there's no tag)
    var distance = this.ctx.distanceFromTag();
    var prerelease = String.format("alpha.0.%d", distance);
    var shortSha = MoreObjects.firstNonNull(this.ctx.shortSha(), UNKNOWN);
    var metadata = String.format("git.%d.%s", distance, shortSha);

    return Semver.ZERO.withIncPatch().withClearedPreRelease().withPreRelease(prerelease).withBuild(metadata);
  }
}
