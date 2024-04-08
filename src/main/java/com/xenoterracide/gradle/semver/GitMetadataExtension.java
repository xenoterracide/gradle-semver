// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.xenoterracide.tools.java.function.ExceptionTools;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The type Git metadata extension.
 */
public class GitMetadataExtension {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final String HEAD = "HEAD";

  private final Supplier<Try.WithResources1<Git>> git;

  GitMetadataExtension(Supplier<Try.WithResources1<Git>> git) {
    this.git = Objects.requireNonNull(git);
  }

  Try<Repository> gitRepository() {
    return this.git.get().of(Git::getRepository).onFailure(e -> {});
  }

  /**
   * Gets branch.
   *
   * @return the branch
   */
  public @Nullable String getBranch() {
    return this.gitRepository().mapTry(Repository::getBranch).getOrNull();
  }

  /**
   * Gets object id for.
   *
   * @param shalike the shalike
   * @return the object id for
   */
  Try<ObjectId> getObjectIdFor(@NonNull String shalike) {
    return this.gitRepository().mapTry(r -> r.resolve(Objects.requireNonNull(shalike)));
  }

  /**
   * Gets rev.
   *
   * @param shalike the shalike
   * @return the rev
   */
  public @Nullable String getRev(@NonNull String shalike) {
    return this.getObjectIdFor(shalike).map(AnyObjectId::getName).getOrNull();
  }

  /**
   * Gets commit.
   *
   * @return the commit
   */
  public @Nullable String getCommit() {
    return this.getRev(HEAD);
  }

  /**
   * Gets commit short.
   *
   * @return the commit short
   */
  public @Nullable String getCommitShort() {
    return this.getObjectIdFor(HEAD).map(o -> o.abbreviate(7)).map(AbbreviatedObjectId::name).getOrNull();
  }

  /**
   * Gets latest tag.
   *
   * @return the latest tag
   */
  public @Nullable String getLatestTag() {
    return this.git.get()
      .of(git -> git.describe().setMatch(VERSION_GLOB))
      .mapTry(DescribeCommand::call)
      .onFailure(e -> {})
      .getOrNull();
  }

  /**
   * Gets describe.
   *
   * @return the describe
   */
  public @Nullable String getDescribe() {
    return this.git.get().of(Git::describe).mapTry(DescribeCommand::call).onFailure(e -> {}).getOrNull();
  }

  /**
   * Gets commit distance.
   *
   * @return the commit distance
   */
  public int getCommitDistance() {
    return this.git.get()
      .of(Git::describe)
      .mapTry(DescribeCommand::call)
      .onFailure(e -> {})
      .map(d -> Iterables.get(Splitter.on('-').split(d), 1))
      .map(Integer::parseInt)
      .onFailure(e -> {}) // expecting Iterables to throw if on tag
      .getOrElse(0);
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public GitStatus getStatus() {
    return this.git.get()
      .of(Git::status)
      .mapTry(StatusCommand::call)
      .onFailure(e -> {})
      .map(Status::isClean)
      .map(clean -> clean ? GitStatus.CLEAN : GitStatus.DIRTY) // flip, dirty is the porcelain option
      .getOrElseThrow(ExceptionTools::toRuntime);
  }
}
