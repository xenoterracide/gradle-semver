// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.BranchOutput;
import com.xenoterracide.gradle.git.GitRemote;
import com.xenoterracide.gradle.git.GitStatus;
import com.xenoterracide.gradle.git.HeadBranchNotAvailable;
import com.xenoterracide.gradle.git.RemoteForHeadBranch;
import com.xenoterracide.tools.java.function.PredicateTools;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Constants;
import org.semver4j.Semver;

final class SemverBuilder {

  private static final String ALPHA = "alpha";
  private static final String SEMVER_DELIMITER = ".";
  private static final String ZERO = "0";

  private final Function<String, Long> distanceCalculator;
  private BranchOutput branchOutput = BranchOutput.NON_HEAD_BRANCH_OR_THROW;
  private RemoteForHeadBranch remoteForHeadBranch = RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW;
  private String remote = "origin";
  private Semver semver;
  private boolean dirtyOut;

  SemverBuilder(Function<String, Long> distanceCalculator, Semver semver) {
    this.distanceCalculator = distanceCalculator;
    this.semver = semver;
  }

  private void createPreRelease() {
    var distance = this.gitMetadata.distance();
    if (distance > 0) {
      if (this.semver.getPreRelease().isEmpty()) { // 1.0 or notag
        this.semver = this.semver.withIncPatch()
          .withPreRelease(String.join(SEMVER_DELIMITER, ALPHA, ZERO, Long.toString(distance)));
      } else { // rc.1
        var preRelease = Stream.concat(
          this.semver.getPreRelease().stream(),
          Stream.of(Long.toString(distance))
        ).collect(Collectors.joining(SEMVER_DELIMITER));
        this.semver = this.semver.withClearedPreRelease().withPreRelease(preRelease);
      }
    }
    if (this.semver.getMajor() == 0 && this.semver.getMinor() == 0 && this.semver.getPatch() == 0) {
      this.semver = this.semver.withPreRelease(String.join(SEMVER_DELIMITER, ALPHA, ZERO, Long.toString(distance)));
    }
  }

  boolean doesNotHaveHeadBranch() {
    return this.gitMetadata.remotes().stream().map(GitRemote::headBranch).noneMatch(Objects::nonNull);
  }

  String getHeadBranch() {
    var matchesRemote =
      this.gitMetadata.remotes()
        .stream()
        .filter(PredicateTools.is(GitRemote::name, Predicate.isEqual(this.remote)))
        .map(GitRemote::headBranch)
        .filter(Objects::nonNull)
        .findAny();
    switch (this.remoteForHeadBranch) {
      case CONFIGURED_ORIGIN_OR_THROW:
        return matchesRemote.orElseThrow();
      case CONFIGURED_ORIGIN_OR_FIRST:
        return matchesRemote.orElseGet(() ->
          this.gitMetadata.remotes()
            .stream()
            .map(GitRemote::headBranch)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow()
        );
      default:
        throw new IllegalStateException("remoteForHeadBranch: " + this.remoteForHeadBranch);
    }
  }

  boolean branchMatchesHeadBranch() {
    var headBranch = StringUtils.removeStart(this.getHeadBranch(), Constants.R_REMOTES);
    return this.gitMetadata.branch();
  }

  Optional<String> getBranch() {
    if (this.branchOutput == BranchOutput.ALWAYS) return Optional.ofNullable(this.gitMetadata.branch());
    if (this.branchOutput == BranchOutput.NONE || this.doesNotHaveHeadBranch()) return Optional.empty();

    return Optional.ofNullable(this.gitMetadata.branch());
  }

  private void createBuild() {
    var distance = this.getDistance();
    if (distance > 0) {
      var sha = Optional.ofNullable(this.gitMetadata.uniqueShort()).map(s -> "g" + s);
      var status = Optional.ofNullable(this.dirtyOut ? this.gitMetadata.status() : null)
        .filter(s -> s == GitStatus.DIRTY)
        .map(Object::toString);
      this.semver = sha
        .map(s -> status.map(sta -> String.join(SEMVER_DELIMITER, s, sta)).orElse(s))
        .map(s -> this.semver.withBuild(s))
        .orElse(this.semver);
    }
  }

  SemverBuilder withDirtyOut(boolean dirtyOut) {
    this.dirtyOut = dirtyOut;
    return this;
  }

  SemverBuilder withBranchOutput(BranchOutput branchOutput) {
    this.branchOutput = branchOutput;
    return this;
  }

  SemverBuilder withRemote(String remote) {
    this.remote = remote;
    return this;
  }

  SemverBuilder withRemoteForHeadBranchConfig(RemoteForHeadBranch remoteForHeadBranch) {
    this.remoteForHeadBranch = remoteForHeadBranch;
    return this;
  }

  Semver build() throws HeadBranchNotAvailable {
    this.createPreRelease();
    this.createBuild();
    return this.semver;
  }

  long getDistance() {
    if (this.branchOutput == BranchOutput.NONE || this.doesNotHaveHeadBranch()) {
      return this.gitMetadata.distance();
    }
    return this.distanceCalculator.apply(this.getHeadBranch());
  }
}
