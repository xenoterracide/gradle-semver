// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

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

  // private BranchOutput branchOutput = BranchOutput.NON_HEAD_BRANCH_OR_THROW;
  // private RemoteForHeadBranch remoteForHeadBranch = RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW;
  // private String remote = "origin";
  private Semver semver;
  private boolean dirtyOut;
  private long distance;
  private @Nullable String uniqueShort;
  private @Nullable GitStatus status;
  private @Nullable String branch;

  SemverBuilder(Semver semver) {
    this.semver = semver;
  }

  static String semverJoin(String... parts) {
    return String.join(SEMVER_DELIMITER, parts);
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

  private void createPreRelease() {
    if (this.distance > 0) {
      if (this.semver.getPreRelease().isEmpty()) { // 1.0 or notag
        this.semver = this.semver.withIncPatch().withPreRelease(semverJoin(ALPHA, ZERO, Long.toString(this.distance)));
      } else { // rc.1
        var preRelease = Stream.concat(
          this.semver.getPreRelease().stream(),
          Stream.of(Long.toString(this.distance))
        ).collect(Collectors.joining(SEMVER_DELIMITER));
        this.semver = this.semver.withClearedPreRelease().withPreRelease(preRelease);
      }
    }
    if (this.semver.getMajor() == 0 && this.semver.getMinor() == 0 && this.semver.getPatch() == 0) {
      this.semver = this.semver.withPreRelease(semverJoin(ALPHA, ZERO, Long.toString(this.distance)));
    }
  }

  private Optional<String> createBuild() {
    if (this.distance > 0) {
      var optSha = Optional.ofNullable(this.uniqueShort);

      return optSha.map(sha -> {
        var g = Optional.of("git");
        var distance = Optional.of(this.distance).map(l -> Long.toString(l));
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
