// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import org.semver4j.Semver;

/**
 * Strategy: No tags exist in the repository, on the HEAD branch.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>New repo with 5 commits on main → {@code 0.0.1-alpha.0.5}</li>
 * </ul>
 *
 * <p>Starts from 0.0.0 and adds prerelease with total commits.</p>
 */
final class NoTagHeadStrategy implements VersionStrategy {

  @Override
  public Semver calculate(GitContext ctx) {
    // Start from 0.0.0, add prerelease with total commits
    // On HEAD branch, distanceFromTag represents total commits (since there's no tag)
    var prerelease = String.format("alpha.0.%d", ctx.distanceFromTag());

    return Semver.ZERO.withIncPatch().withClearedPreRelease().withPreRelease(prerelease);
  }
}
