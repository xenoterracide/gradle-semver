// SPDX-FileCopyrightText: Copyright © 2024 - 2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitStatus;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RegExUtils;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

final class SemverBuilder {

  private static final String ALPHA = "alpha";
  private static final String SEMVER_DELIMITER = ".";
  private static final String ZERO = "0";

  private Semver semver;
  private boolean dirtyOut;
  private long preReleaseDistance;
  private long buildDistance;
  private @Nullable String uniqueShort;
  private @Nullable GitStatus status;
  private @Nullable String branch;

  SemverBuilder(Semver semver) {
    this.semver = semver;
  }

  static String semverJoin(String... parts) {
    return String.join(SEMVER_DELIMITER, parts);
  }

  private void createPreRelease() {
    if (this.preReleaseDistance > 0) {
      if (this.semver.getPreRelease().isEmpty()) {
        // 1.0 or notag
        this.semver = this.semver.withIncPatch().withPreRelease(
          semverJoin(ALPHA, ZERO, Long.toString(this.preReleaseDistance))
        );
      } else {
        // rc.1
        var preRelease = Stream.concat(
          this.semver.getPreRelease().stream(),
          Stream.of(Long.toString(this.preReleaseDistance))
        ).collect(Collectors.joining(SEMVER_DELIMITER));
        this.semver = this.semver.withClearedPreRelease().withPreRelease(preRelease);
      }
    }

    // When starting at 0.0.0, we always emit an alpha prerelease (including distance 0)
    // so the "no repo" fallback is `0.0.0-alpha.0.0`.
    if (this.semver.getMajor() == 0 && this.semver.getMinor() == 0 && this.semver.getPatch() == 0) {
      this.semver = this.semver.withPreRelease(semverJoin(ALPHA, ZERO, Long.toString(this.preReleaseDistance)));
    }
  }

  /**
   * @implNote `+git.<distance>.<sha>` must be keyed off *distance from the nearest tag* (i.e. `git describe --long`),
   *   not off the prerelease distance.
   *   <p>
   *   Why? Our prerelease distance can be configured to represent *distance from HEAD branch* (merge-base)
   *   for non-head branches, where it is valid (and expected) for prereleaseDistance to be 0 while the
   *   tag distance is > 0. In that case we still want to emit build metadata that reflects the true
   *   commits-since-tag count.
   *   <p>
   *   If we used prereleaseDistance here, then a repo at `vX.Y.Z-rc.1-3-g<sha>` on the head branch could
   *   incorrectly produce `X.Y.Z-rc.1` (dropping `.3+git.3.<sha>`), which is exactly the bug we fixed.
   */
  private Optional<String> createBuild() {
    if (this.buildDistance > 0) {
      var optSha = Optional.ofNullable(this.uniqueShort);

      return optSha.map(sha -> {
        var g = Optional.of("git");
        var distance = Optional.of(this.buildDistance).map(l -> Long.toString(l));
        var branch = Optional.ofNullable(this.branch);
        var hasBranch = branch.map(b -> "branch");
        var status = Optional.ofNullable(this.dirtyOut ? this.status : null)
          .filter(s -> s == GitStatus.DIRTY)
          .map(Object::toString);

        return Stream.of(hasBranch, branch, g, distance, optSha, status)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.joining(SEMVER_DELIMITER));
      });
    }
    return Optional.empty();
  }

  SemverBuilder withDirtyOut(boolean dirtyOut) {
    this.dirtyOut = dirtyOut;
    return this;
  }

  SemverBuilder withUniqueShort(@Nullable String uniqueShort) {
    this.uniqueShort = uniqueShort;
    return this;
  }

  SemverBuilder withPreReleaseDistance(long distance) {
    this.preReleaseDistance = distance;
    return this;
  }

  SemverBuilder withBuildDistance(long distance) {
    this.buildDistance = distance;
    return this;
  }

  SemverBuilder withGitStatus(GitStatus status) {
    this.status = status;
    return this;
  }

  SemverBuilder withBranch(@Nullable String branch) {
    this.branch = RegExUtils.replaceAll(branch, "\\P{Alnum}", "-");
    return this;
  }

  Semver build() {
    this.createPreRelease();
    this.createBuild().ifPresent(build -> this.semver = this.semver.withBuild(build));
    return this.semver;
  }
}
