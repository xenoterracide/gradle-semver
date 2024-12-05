// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.xenoterracide.gradle.semver.internal.ExceptionTools;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Git metadata extension.
 */
public class GitMetadataExtension implements GitMetadata {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final Splitter DESCRIBE_SPLITTER = Splitter.on('-');
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Supplier<Try<Git>> git;

  /**
   * Instantiates a new Git metadata extension.
   *
   * @param git
   *   a jgit instance supplier
   */
  public GitMetadataExtension(Supplier<Try<Git>> git) {
    this.git = git;
  }

  static <T> Function<? super Throwable, ? extends T> allWith(@Nullable T value) {
    return e ->
      Match(e)
        .option(
          Case($(instanceOf(RefNotFoundException.class)), value),
          Case($(instanceOf(RepositoryNotFoundException.class)), value)
        )
        .getOrElseThrow(() -> ExceptionTools.toRuntime(e));
  }

  <R> Try<R> tryCommand(CheckedFunction1<Git, R> command) {
    return this.git.get().mapTry(command).onFailure(e -> this.log.debug("failed", e)).filter(Objects::nonNull);
  }

  Try<Repository> gitRepository() {
    return this.tryCommand(Git::getRepository);
  }

  Try<@Nullable String> describe() {
    return this.tryCommand(g -> g.describe().setMatch(VERSION_GLOB).setTags(true))
      .mapTry(DescribeCommand::call)
      .recover(NoSuchElementException.class, e -> null);
  }

  Try<LogCommand> gitLog() {
    return this.tryCommand(Git::log);
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
   *   the shalike
   * @return the object id for
   */
  Try<ObjectId> getObjectIdFor(@NonNull String shalike) {
    return this.gitRepository().mapTry(r -> r.resolve(Objects.requireNonNull(shalike)));
  }

  /**
   * Gets rev.
   *
   * @param shalike
   *   the shalike
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

  @Override
  public @Nullable String uniqueShort() {
    return this.gitRepository()
      .mapTry(Repository::newObjectReader)
      .mapTry(reader -> reader.abbreviate(this.getObjectIdFor(Constants.HEAD).get(), 8))
      .map(AbbreviatedObjectId::name)
      .recover(NoSuchElementException.class, e -> null)
      .onFailure(e -> this.log.error("failed to get unique short", e))
      .getOrNull();
  }

  @Override
  public @Nullable String tag() {
    return this.tryCommand(g -> g.describe().setMatch(VERSION_GLOB).setAbbrev(0))
      .mapTry(DescribeCommand::call)
      .recover(NoSuchElementException.class, e -> null)
      .recover(GitMetadataExtension.allWith(null))
      .onFailure(e -> this.log.error("failed to get tag", e))
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

  long shortCount() {
    return this.gitLog()
      .map(l -> l.setMaxCount(5))
      .mapTry(LogCommand::call)
      .map(i -> StreamSupport.stream(i.spliterator(), false))
      .map(Stream::count)
      .getOrElse(0L);
  }

  int distanceFromNoTag() {
    return this.gitLog()
      .mapTry(LogCommand::all)
      .mapTry(LogCommand::call)
      .map(i -> StreamSupport.stream(i.spliterator(), false))
      .map(Stream::count)
      .recover(NoSuchElementException.class, 0L)
      .recover(GitMetadataExtension.allWith(0L))
      .onFailure(e -> this.log.error("failed to get distance without a tag", e))
      .map(Long::intValue)
      .get();
  }

  @Override
  public int distance() {
    var shortCount = this.shortCount();
    if (shortCount < 4) {
      this.log.warn("shallow clone detected! git only has {} commits", shortCount);
    }
    return this.describe()
      .filter(Objects::nonNull)
      .map(d -> {
        var ary = Iterables.toArray(DESCRIBE_SPLITTER.split(d), String.class);
        return ary.length > 2 ? ary[ary.length - 2] : "0";
      })
      .map(Integer::parseInt)
      .recover(GitMetadataExtension.allWith(0))
      .recover(NoSuchElementException.class, e -> this.distanceFromNoTag())
      .onFailure(e -> this.log.error("failed to get distance", e))
      .getOrElse(0);
  }

  @Override
  public GitStatus status() {
    return this.tryCommand(Git::status)
      .mapTry(StatusCommand::call)
      .filter(Objects::nonNull)
      .map(status -> status.isClean() ? GitStatus.CLEAN : GitStatus.DIRTY)
      .recover(NoSuchElementException.class, e -> GitStatus.NO_REPO)
      .recover(RepositoryNotFoundException.class, e -> GitStatus.NO_REPO)
      .onFailure(e -> this.log.error("failed to get status", e))
      .getOrElseThrow(ExceptionTools::toRuntime);
  }
}
