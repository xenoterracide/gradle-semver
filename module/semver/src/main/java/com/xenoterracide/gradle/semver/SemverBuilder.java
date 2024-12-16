// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.GitMetadata;
import com.xenoterracide.tools.java.function.PredicateTools;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

final class SemverBuilder {

  static final String ALPHA = "alpha";
  static final String SEMVER_DELIMITER = ".";
  private static final String PRE_VERSION = "0.0.0";
  private static final String ZERO = "0";

  private final GitMetadata gitMetadata;
  private BranchOutput branchOutput = BranchOutput.NON_HEAD_BRANCH_OR_THROW;
  private RemoteForHeadBranch remoteForHeadBranch = RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW;
  private String remote = "origin";
  private Semver semver;
  private boolean dirtyOut;

  SemverBuilder(GitMetadata gitMetadata) {
    this.gitMetadata = gitMetadata;
    this.semver = new Semver(tagFrom(this.gitMetadata.tag()));
  }

  static String tagFrom(@Nullable String vString) {
    return vString == null ? PRE_VERSION : vString.substring(1);
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

  boolean hasHeadBranch() {
    return this.gitMetadata.remotes().stream().map(GitRemote::headBranch).anyMatch(Objects::nonNull);
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

  Optional<String> getBranch() throws HeadBranchNotAvailable {
    var headBranch = this.getHeadBranch();
    switch (this.branchOutput) {
      case NON_HEAD_BRANCH_OR_THROW:
        if (this.hasHeadBranch() && !Objects.equals(this.gitMetadata.branch(), headBranch)) {
          return Optional.ofNullable(this.gitMetadata.branch());
        }
        throw new HeadBranchNotAvailable();
      case NON_HEAD_BRANCH_FALLBACK_ALWAYS:
        if (this.hasHeadBranch() && Objects.equals(this.gitMetadata.branch(), headBranch)) {
          return Optional.empty();
        }
        return Optional.ofNullable(this.gitMetadata.branch());
      case NON_HEAD_BRANCH_FALLBACK_NONE:
        if (this.hasHeadBranch() && !Objects.equals(this.gitMetadata.branch(), headBranch)) {
          return Optional.ofNullable(this.gitMetadata.branch());
        }
      // fallthrough
      case NONE:
        return Optional.empty();
      default:
        throw new IllegalStateException("branchOutput: " + this.branchOutput);
    }
  }

  private void createBuild() throws HeadBranchNotAvailable {
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
    if (this.branchOutput == BranchOutput.NONE || !this.hasHeadBranch()) {
      return this.gitMetadata.distance();
    }
    return 0L;
  }
}
