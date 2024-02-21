// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import java.util.Objects;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;

class PorcelainGit implements VersionDetails {

  private static final String VERSION_PREFIX = "v";
  private static final String VERSION_GLOB = VERSION_PREFIX + "[0-9]*.[0-9]*.[0-9]*";

  private final Git git;

  PorcelainGit(@NonNull Git git) {
    this.git = Objects.requireNonNull(git);
  }

  String describe() {
    try {
      return git.describe().setMatch(VERSION_GLOB).call();
    } catch (GitAPIException | InvalidPatternException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @Nullable String getBranchName() {
    return null;
  }

  @Override
  public @Nullable String getGitHashFull() {
    return null;
  }

  @Override
  public @Nullable String getGitHash() {
    return null;
  }

  @Override
  public @Nullable String getLastTag() {
    return null;
  }

  @Override
  public int getCommitDistance() {
    return 0;
  }

  @Override
  public boolean getIsCleanTag() {
    return Try.ofCallable(git.status()).map(Status::isClean).getOrElseThrow(ExceptionTools::rethrow);
  }

  @Override
  public @Nullable String getVersion() {
    return Try
      .of(() -> git.describe().setMatch(VERSION_GLOB))
      .mapTry(DescribeCommand::call)
      .getOrElseThrow(ExceptionTools::rethrow);
  }
}
