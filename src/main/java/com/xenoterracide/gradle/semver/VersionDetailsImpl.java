// SPDX-License-Identifier: Apache-2.0
// © Copyright 2018-2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import java.util.Objects;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class VersionDetailsImpl implements VersionDetails {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";

  private final Git git;

  VersionDetailsImpl(@NonNull Git git) {
    this.git = Objects.requireNonNull(git);
  }

  Try<Repository> gitRepository() {
    return Try.of(this.git::getRepository).onFailure(ExceptionTools::rethrow);
  }

  @Override
  public String getBranchName() {
    return this.gitRepository().mapTry(Repository::getBranch).getOrNull();
  }

  @Override
  public String getGitHashFull() {
    return this.gitRepository().mapTry(r -> r.resolve("HEAD")).map(AnyObjectId::getName).getOrNull();
  }

  @Override
  public String getGitHash() {
    return this.gitRepository()
      .mapTry(r -> r.resolve("HEAD"))
      .map(o -> o.abbreviate(7))
      .map(AbbreviatedObjectId::name)
      .getOrNull();
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
  public @Nullable String getDescribe() {
    return Try.of(() -> git.describe().call()).onFailure(ExceptionTools::rethrow).getOrNull();
  }

  @Override
  public int getCommitDistance() {
    return 0;
  }

  @Override
  public boolean getIsCleanTag() {
    return Try.ofCallable(git.status()).map(Status::isClean).getOrElseThrow(ExceptionTools::rethrow);
  }
}
