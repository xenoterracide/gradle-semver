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
  private RemoteForHeadBranch remoteForHeadBranchConfig = RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW;
  private String remoteForHeadBranch = "origin";
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

  boolean hasFirstHeadBranch() {
    return this.gitMetadata.remotes().stream().map(GitRemote::headBranch).anyMatch(Objects::nonNull);
  }

  String getHeadBranch() {
    switch (this.remoteForHeadBranchConfig) {
      case CONFIGURED_ORIGIN_OR_THROW:
        return this.gitMetadata.remotes()
          .stream()
          .filter(PredicateTools.prop(GitRemote::name, Predicate.isEqual(this.remoteForHeadBranch)))
          .map(GitRemote::headBranch)
          .filter(Objects::nonNull)
          .findAny()
          .orElseThrow();
      case CONFIGURED_ORIGIN_OR_FIRST:
        return this.gitMetadata.remotes()
          .stream()
          .filter(PredicateTools.prop(GitRemote::name, Predicate.isEqual(this.remoteForHeadBranch)))
          .map(GitRemote::headBranch)
          .filter(Objects::nonNull)
          .findAny()
          .orElseGet(() ->
            this.gitMetadata.remotes()
              .stream()
              .map(GitRemote::headBranch)
              .filter(Objects::nonNull)
              .findFirst()
              .orElseThrow()
          );
      default:
        throw new IllegalStateException("should not be reachable");
    }
  }

  Optional<String> getBranch() throws HeadBranchNotAvailable {
    switch (this.branchOutput) {
      case NON_HEAD_BRANCH_OR_THROW:
        if (this.hasFirstHeadBranch() && !Objects.equals(this.gitMetadata.branch(), this.getHeadBranch())) {
          return Optional.ofNullable(this.gitMetadata.branch());
        }
      case NON_HEAD_BRANCH_FALLBACK_ALWAYS:
      case NON_HEAD_BRANCH_FALLBACK_NONE:
        return Optional.ofNullable(this.gitMetadata.branch());
      case NONE:
        return Optional.empty();
      default:
        throw new HeadBranchNotAvailable();
    }
  }

  private void createBuild() throws HeadBranchNotAvailable {
    var distance = this.gitMetadata.distance();
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

  SemverBuilder withRemoteForHeadBranch(String remoteForHeadBranch) {
    this.remoteForHeadBranch = remoteForHeadBranch;
    return this;
  }

  SemverBuilder withRemoteForHeadBranchConfig(RemoteForHeadBranch remoteForHeadBranchConfig) {
    this.remoteForHeadBranchConfig = remoteForHeadBranchConfig;
    return this;
  }

  Semver build() throws HeadBranchNotAvailable {
    this.createPreRelease();
    this.createBuild();
    return this.semver;
  }
}
