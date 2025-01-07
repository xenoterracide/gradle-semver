// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitStatus;
import com.xenoterracide.gradle.git.HeadBranchNotAvailable;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

final class SemverBuilder {

  private static final String ALPHA = "alpha";
  private static final String SEMVER_DELIMITER = ".";
  private static final String ZERO = "0";

  // private BranchOutput branchOutput = BranchOutput.NON_HEAD_BRANCH_OR_THROW;
  // private RemoteForHeadBranch remoteForHeadBranch = RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW;
  // private String remote = "origin";
  private Semver semver;
  private boolean dirtyOut;
  private long distance;
  private @Nullable String uniqueShort;
  private @Nullable GitStatus status;

  SemverBuilder(Semver semver) {
    this.semver = semver;
  }

  private void createPreRelease() {
    if (this.distance > 0) {
      if (this.semver.getPreRelease().isEmpty()) { // 1.0 or notag
        this.semver = this.semver.withIncPatch()
          .withPreRelease(String.join(SEMVER_DELIMITER, ALPHA, ZERO, Long.toString(this.distance)));
      } else { // rc.1
        var preRelease = Stream.concat(
          this.semver.getPreRelease().stream(),
          Stream.of(Long.toString(this.distance))
        ).collect(Collectors.joining(SEMVER_DELIMITER));
        this.semver = this.semver.withClearedPreRelease().withPreRelease(preRelease);
      }
    }
    if (this.semver.getMajor() == 0 && this.semver.getMinor() == 0 && this.semver.getPatch() == 0) {
      this.semver = this.semver.withPreRelease(
          String.join(SEMVER_DELIMITER, ALPHA, ZERO, Long.toString(this.distance))
        );
    }
  }

  /*
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

   */

  private Optional<String> createBuild() {
    if (this.distance > 0) {
      var optSha = Optional.ofNullable(this.uniqueShort).map(s -> "g" + s);
      var status = Optional.ofNullable(this.dirtyOut ? this.status : null)
        .filter(s -> s == GitStatus.DIRTY)
        .map(Object::toString);

      return optSha.map(sha -> status.map(sta -> String.join(SEMVER_DELIMITER, sha, sta)).orElse(sha));
    }
    return Optional.empty();
  }

  SemverBuilder withDirtyOut(boolean dirtyOut) {
    this.dirtyOut = dirtyOut;
    return this;
  }

  /*
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
*/
  SemverBuilder withUniqueShort(@Nullable String uniqueShort) {
    this.uniqueShort = uniqueShort;
    return this;
  }

  SemverBuilder withDistance(long distance) {
    this.distance = distance;
    return this;
  }

  SemverBuilder withGitStatus(GitStatus status) {
    this.status = status;
    return this;
  }

  Semver build() throws HeadBranchNotAvailable {
    this.createPreRelease();
    this.createBuild().ifPresent(build -> this.semver = this.semver.withBuild(build));
    return this.semver;
  }
}
