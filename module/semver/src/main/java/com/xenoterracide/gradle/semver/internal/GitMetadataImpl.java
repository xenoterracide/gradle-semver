// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.RemoteListCommand;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.RefNotFoundException;
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

  private final Supplier<Optional<Git>> git;

  /**
   * Instantiates a new Git metadata extension.
   *
   * @param git
   *   a jgit instance supplier
   */
  public GitMetadataImpl(Supplier<Optional<Git>> git) {
    this.git = git;
  }

  <R> Try<R> tryCommand(CheckedFunction1<Git, R> supplier) {
    return this.git.get()
      .map(g -> Try.of(() -> supplier.apply(g)))
      .orElseGet(NoGitDirException::failure)
      .recover(NoGitDirException.class, e -> null)
      .onFailure(e -> this.log.debug("failed", e));
  }

  Try<Repository> gitRepository() {
    return this.tryCommand(Git::getRepository);
  }

  Try<@Nullable String> describe() {
    return this.git.get()
      .map(g -> Try.of(() -> g.describe().setMatch(VERSION_GLOB).setTags(true)))
      .orElseGet(NoGitDirException::failure)
      .mapTry(DescribeCommand::call)
      .recover(NoGitDirException.class, e -> null)
      .onFailure(e -> this.log.debug("failed to get describe", e));
  }

  Try<LogCommand> gitLog() {
    return this.git.get().map(g -> Try.of(g::log)).orElseGet(NoGitDirException::failure);
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
      .mapTry(objectReader -> objectReader.abbreviate(this.getObjectIdFor(Constants.HEAD).get(), 8))
      .map(AbbreviatedObjectId::name)
      .onFailure(e -> this.log.debug("failed to get unique short", e))
      .getOrNull();
  }

  @Override
  public @Nullable String tag() {
    return this.git.get()
      .map(g -> Try.of(() -> g.describe().setMatch(VERSION_GLOB).setAbbrev(0)))
      .orElseGet(NoGitDirException::failure)
      .mapTry(DescribeCommand::call)
      .onFailure(e -> this.log.debug("failed to get tag", e))
      .getOrNull();
  }

  private int distanceFromNoCommit() {
    return this.gitLog()
      .mapTry(LogCommand::all)
      .mapTry(LogCommand::call)
      .map(iter -> StreamSupport.stream(iter.spliterator(), false).count())
      .map(Long::intValue)
      .onFailure(e -> this.log.debug("failed to get distance from no commit", e))
      .get();
  }

  @Override
  public int distance() {
    return this.describe()
      .filter(Objects::nonNull)
      .map(d -> {
        var ary = Iterables.toArray(DESCRIBE_SPLITTER.split(d), String.class);
        return ary.length > 2 ? ary[ary.length - 2] : "0";
      })
      .map(split -> Integer.parseInt(split))
      .recover(NoGitDirException.class, 0)
      .recover(RefNotFoundException.class, 0)
      .recover(NoSuchElementException.class, e -> this.distanceFromNoCommit())
      .onFailure(e -> this.log.debug("failed to get distance", e))
      .getOrElse(0);
  }

  @Override
  public GitStatus status() {
    return this.git.get()
      .map(g -> Try.of(g::status))
      .orElseGet(NoGitDirException::failure)
      .filter(Objects::nonNull)
      .mapTry(StatusCommand::call)
      .recover(NoGitDirException.class, e -> null)
      // flip, dirty is the porcelain option.
      .map(status -> status == null ? GitStatus.NO_REPO : status.isClean() ? GitStatus.CLEAN : GitStatus.DIRTY)
      .onFailure(e -> this.log.debug("failed to get status", e))
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
      .onFailure(e -> this.log.debug("failed to get remotes", e))
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
      .onFailure(e -> this.log.debug("failed to get distance", e))
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
