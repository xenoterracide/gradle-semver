// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import java.util.Objects;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

class PorcelainGit implements VersionDetails {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final String PRE_VERSION = "0.0.0";
  private static final String SNAPSHOT = "SNAPSHOT";

  private final Git git;

  PorcelainGit(@NonNull Git git) {
    this.git = Objects.requireNonNull(git);
  }

  @Override
  public @Nullable String getLastTag() {
    return Try
      .of(() -> git.describe().setMatch(VERSION_GLOB))
      .mapTry(DescribeCommand::call)
      .onFailure(ExceptionTools::rethrow)
      .getOrNull();
  }

  @Override
  public boolean getIsCleanTag() {
    return Try.ofCallable(git.status()).map(Status::isClean).getOrElseThrow(ExceptionTools::rethrow);
  }

  @Override
  public Semver getSemver() {
    return Try
      .of(() -> git.describe().setMatch(VERSION_GLOB))
      .mapTry(DescribeCommand::call)
      .onFailure(ExceptionTools::rethrow)
      .map(v -> null == v ? PRE_VERSION : v)
      .map(Semver::coerce)
      .map(v -> Objects.equals(v.getVersion(), PRE_VERSION) ? v.withPreRelease(SNAPSHOT) : v)
      .map(v ->
        v
          .getPreRelease()
          .stream()
          .filter(p -> p.matches("^\\d+-+g\\p{XDigit}{7}$"))
          .findFirst()
          .map(p -> v.withClearedPreRelease().withPreRelease(SNAPSHOT).withBuild(p))
          .orElse(v)
      )
      .map(v -> new MavenSemver(v.getVersion()))
      .get();
  }

  static class MavenSemver extends Semver {

    MavenSemver(@NotNull String version) {
      super(version);
    }

    @Override
    public String getVersion() {
      return super.getVersion().replace("+", "-");
    }
  }
}
