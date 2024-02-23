// SPDX-License-Identifier: Apache-2.0
// © Copyright 2018-2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GitMetadataExtension {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final String HEAD = "HEAD";

  private final Supplier<Git> git;

  GitMetadataExtension(Supplier<Git> git) {
    this.git = Objects.requireNonNull(git);
  }

  Try<Repository> gitRepository() {
    return Try.of(() -> this.git.get().getRepository()).onFailure(ExceptionTools::rethrow);
  }

  public @Nullable String getBranch() {
    return this.gitRepository().mapTry(Repository::getBranch).getOrNull();
  }

  Try<ObjectId> getObjectIdFor(@NonNull String shalike) {
    return this.gitRepository().mapTry(r -> r.resolve(Objects.requireNonNull(shalike)));
  }

  public @Nullable String getRev(@NonNull String shalike) {
    return this.getObjectIdFor(shalike).map(AnyObjectId::getName).getOrNull();
  }

  public @Nullable String getCommit() {
    return this.getRev(HEAD);
  }

  public @Nullable String getCommitShort() {
    return this.getObjectIdFor(HEAD).map(o -> o.abbreviate(7)).map(AbbreviatedObjectId::name).getOrNull();
  }

  public @Nullable String getLatestTag() {
    return Try
      .of(() -> git.get().describe().setMatch(VERSION_GLOB))
      .mapTry(DescribeCommand::call)
      .onFailure(ExceptionTools::rethrow)
      .getOrNull();
  }

  public @Nullable String getDescribe() {
    return Try.ofCallable(git.get().describe()).onFailure(ExceptionTools::rethrow).getOrNull();
  }

  public int getCommitDistance() {
    return Try
      .ofCallable(git.get().describe())
      .onFailure(ExceptionTools::rethrow)
      .map(d -> Iterables.get(Splitter.on('-').split(d), 1))
      .map(Integer::parseInt)
      .getOrElse(0);
  }

  public GitStatus getStatus() {
    return Try
      .ofCallable(git.get().status())
      .map(Status::isClean)
      .map(clean -> clean ? GitStatus.CLEAN : GitStatus.DIRTY) // flip, dirty is the porcelain option
      .getOrElseThrow(ExceptionTools::rethrow);
  }
}
