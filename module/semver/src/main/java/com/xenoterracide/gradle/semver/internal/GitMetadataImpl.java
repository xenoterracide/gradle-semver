// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.xenoterracide.gradle.semver.GitRemote;
import com.xenoterracide.gradle.semver.GitStatus;
import com.xenoterracide.tools.java.function.ExceptionTools;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.RemoteListCommand;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Git metadata extension.
 */
public class GitMetadataImpl implements GitMetadata {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final Splitter DESCRIBE_SPLITTER = Splitter.on('-');
  private static final Splitter REF_SPLITTER = Splitter.on('/');
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Supplier<Try<Git>> git;

  /**
   * Instantiates a new Git metadata extension.
   *
   * @param git
   *   a jgit instance supplier
   */
  public GitMetadataImpl(Supplier<Try<Git>> git) {
    this.git = git;
  }

  static boolean hasRefs(Git git) {
    return Try.of(() -> git.getRepository().getRefDatabase().hasRefs()).getOrElse(false);
  }

  static <T> Function<? super Throwable, ? extends T> allWith(@Nullable T value) {
    return e ->
      Match(e)
        .option(
          Case($(instanceOf(NoRefsException.class)), value),
          Case($(instanceOf(NoGitDirException.class)), value),
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
    return this.tryCommand(g -> g.describe().setMatch(VERSION_GLOB).setTags(true)).mapTry(DescribeCommand::call);
  }

  Try<LogCommand> gitLog() {
    return this.tryCommand(Git::log);
  }

  /**
   * Gets branch.
   *
   * @return the branch
   */
  @Override
  public @Nullable String branch() {
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
      .recover(GitMetadataImpl.allWith(null))
      .onFailure(e -> this.log.error("failed to get tag", e))
      .getOrNull();
  }

  long shortCount() {
    return this.gitLog()
      .map(l -> l.setMaxCount(5))
      .mapTry(LogCommand::call)
      .map(IterableTools::of)
      .map(Stream::count)
      .getOrElse(0L);
  }

  long distanceFromNoTag() {
    return this.gitLog()
      .mapTry(LogCommand::all)
      .mapTry(LogCommand::call)
      .map(IterableTools::of)
      .map(Stream::count)
      .recover(NoSuchElementException.class, 0L)
      .recover(GitMetadataImpl.allWith(0L))
      .onFailure(e -> this.log.error("failed to get distance without a tag", e))
      .get();
  }

  @Override
  public long distance() {
    var shortCount = this.shortCount();
    if (shortCount < 4) {
      var command = "git fetch --all --filter blob:none";
      this.log.warn(
          "git has {} commits which is less than 5. This is either a new repository or a shallow clone. Run `{}` to get a full history without files or distance may not be accurate.",
          shortCount,
          command
        );
    }
    return this.describe()
      .filter(Objects::nonNull)
      .map(d -> {
        var ary = Iterables.toArray(DESCRIBE_SPLITTER.split(d), String.class);
        return ary.length > 2 ? ary[ary.length - 2] : "0";
      })
      .map(Long::parseLong)
      .recover(GitMetadataImpl.allWith(0L))
      .recover(NoSuchElementException.class, e -> this.distanceFromNoTag())
      .onFailure(e -> this.log.error("failed to get distance", e))
      .getOrElse(0L);
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

  /**
   * Full 40 character commit SHA.
   *
   * @return sha
   */
  @Override
  public @Nullable String commit() {
    return this.getRev(Constants.HEAD);
  }

  @Override
  public List<GitRemote> remotes() {
    return this.tryCommand(Git::remoteList)
      .mapTry(RemoteListCommand::call)
      .map(Collection::stream)
      .map(s -> s.map(RemoteConfig::getName))
      .map(s -> s.filter(Objects::nonNull))
      .map(s -> s.map(name -> RemoteImpl.nullCheck(name, this.headBranch(name))))
      .map(s -> s.collect(Collectors.<GitRemote>toList()))
      .onFailure(e -> this.log.error("failed to get remotes", e))
      .getOrElse(ArrayList::new);
  }

  @Nullable
  String headBranch(String remote) {
    return this.tryCommand(Git::lsRemote)
      .map(ls -> ls.setRemote(remote))
      .mapTry(LsRemoteCommand::callAsMap)
      .map(m -> m.get(Constants.HEAD))
      .filter(Objects::nonNull)
      .map(ref -> ref.getTarget().getName())
      .map(ref -> REF_SPLITTER.splitToList(ref).get(2))
      .onFailure(e -> this.log.error("failed to get HEAD branch", e))
      .getOrNull();
  }

  private static class RemoteImpl implements GitRemote {

    private final String name;
    private final @Nullable String headBranch;

    RemoteImpl(String name, @Nullable String headBranch) {
      this.name = name;
      this.headBranch = headBranch;
    }

    static GitRemote nullCheck(@Nullable String name, @Nullable String headBranch) {
      return new RemoteImpl(Objects.requireNonNull(name), headBranch);
    }

    @Override
    public @Nullable String headBranch() {
      return this.headBranch;
    }

    @Override
    public @NonNull String name() {
      return this.name;
    }
  }
}
