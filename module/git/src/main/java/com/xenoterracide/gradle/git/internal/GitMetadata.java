// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.xenoterracide.gradle.git.GitStatus;
import io.vavr.control.Try;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The type Git metadata extension.
 */
public class GitMetadata {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final Splitter DESCRIBE_SPLITTER = Splitter.on('-');

  private final Supplier<Optional<Git>> git;

  GitMetadata(Supplier<Optional<Git>> git) {
    this.git = git;
  }

  Try<Repository> gitRepository() {
    return this.git.get()
      .map(g -> Try.of(g::getRepository))
      .orElseGet(NoGitDirException::failure)
      .recover(NoGitDirException.class, e -> null);
  }

  Try<@Nullable String> describe() {
    return this.git.get()
      .map(g -> Try.of(() -> g.describe().setMatch(VERSION_GLOB).setTags(true)))
      .orElseGet(NoGitDirException::failure)
      .mapTry(DescribeCommand::call)
      .recover(NoGitDirException.class, e -> null);
  }

  Try<LogCommand> gitLog() {
    return this.git.get().map(g -> Try.of(g::log)).orElseGet(NoGitDirException::failure);
  }

  public Set<String> remotes() {
    return this.gitRepository().map(Repository::getRemoteNames).getOrElse(Set.of());
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
   * @param shalike
   *         the shalike
   * @return the object id for
   */
  Try<ObjectId> getObjectIdFor(@NonNull String shalike) {
    return this.gitRepository().mapTry(r -> r.resolve(Objects.requireNonNull(shalike)));
  }

  /**
   * Gets rev.
   *
   * @param shalike
   *         the shalike
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
    return this.getRev(Constants.HEAD);
  }

  /**
   * Short version of a commit SHA.
   *
   * @return SHA. Length is always 8, regardless of whether it is unique.
   */
  public @Nullable String getCommitShort() {
    return this.getObjectIdFor(Constants.HEAD).map(o -> o.abbreviate(8)).map(AbbreviatedObjectId::name).getOrNull();
  }

  public @Nullable String uniqueShort() {
    return this.gitRepository()
      .mapTry(Repository::newObjectReader)
      .mapTry(objectReader -> objectReader.abbreviate(this.getObjectIdFor(Constants.HEAD).get(), 8))
      .map(AbbreviatedObjectId::name)
      .getOrNull();
  }

  public @Nullable String tag() {
    return this.git.get()
      .map(g -> Try.of(() -> g.describe().setMatch(VERSION_GLOB).setAbbrev(0)))
      .orElseGet(NoGitDirException::failure)
      .mapTry(DescribeCommand::call)
      .getOrNull();
  }

  /**
   * Gets describe.
   *
   * @return the describe
   */
  public @Nullable String getDescribe() {
    return this.describe().getOrNull();
  }

  private int distanceFromNoCommit() {
    return this.gitLog()
      .mapTry(LogCommand::all)
      .mapTry(LogCommand::call)
      .map(iter -> StreamSupport.stream(iter.spliterator(), false).count())
      .map(Long::intValue)
      .get();
  }

  public int distance() {
    return this.describe()
      .filter(Objects::nonNull)
      .map(d -> Iterables.get(DESCRIBE_SPLITTER.split(d), 1))
      .map(Integer::parseInt)
      .recover(RefNotFoundException.class, 0)
      .recover(NoSuchElementException.class, e -> this.distanceFromNoCommit())
      .getOrElse(0);
  }

  public GitStatus status() {
    return this.git.get()
      .map(g -> Try.of(() -> g.status()))
      .orElseGet(NoGitDirException::failure)
      .filter(Objects::nonNull)
      .mapTry(s -> s.call())
      .recover(NoGitDirException.class, e -> null)
      // flip, dirty is the porcelain option.
      .map(status -> status == null ? GitStatus.NO_REPO : status.isClean() ? GitStatus.CLEAN : GitStatus.DIRTY)
      .getOrElseThrow(ExceptionTools::toRuntime);
  }
}
