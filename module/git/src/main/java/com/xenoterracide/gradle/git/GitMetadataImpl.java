// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import com.xenoterracide.tools.java.function.ExceptionTools;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
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
  private static final String GIT_SEPARATOR = "/";
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final TryGit git;

  GitMetadataImpl(TryGit git) {
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

  Try<Repository> gitRepository() {
    return Try.of(this.git::get)
      .filter(Objects::nonNull)
      .map(Git::getRepository)
      .onFailure(e -> this.log.error("failed to get repository", e));
  }

  /**
   * Gets branch.
   *
   * @return the branch
   */
  @Override
  public @Nullable String branch() {
    return this.gitRepository()
      .mapTry(Repository::getBranch)
      .recover(NoSuchElementException.class, e -> null)
      .getOrNull();
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

  @Nullable
  String getRev(@NonNull String shalike) {
    return this.getObjectIdFor(shalike).map(AnyObjectId::getName).getOrNull();
  }

  @Override
  public @Nullable String uniqueShort() {
    return this.gitRepository()
      .mapTry(Repository::newObjectReader)
      .mapTry(reader -> reader.abbreviate(this.getObjectIdFor(Constants.HEAD).get()))
      .map(AbbreviatedObjectId::name)
      .recover(NoSuchElementException.class, e -> null)
      .onFailure(e -> this.log.error("failed to get unique short", e))
      .getOrNull();
  }

  @Override
  public @Nullable String tag() {
    return this.git.tryCommand(g -> g.describe().setMatch(VERSION_GLOB).setAbbrev(0))
      .recover(NoSuchElementException.class, e -> null)
      .recover(GitMetadataImpl.allWith(null))
      .onFailure(e -> this.log.error("failed to get tag", e))
      .getOrNull();
  }

  long shortCount() {
    return this.git.tryCommand(git -> git.log().setMaxCount(5)).map(IterableTools::of).map(Stream::count).getOrElse(0L);
  }

  @Override
  public long distance() {
    var shortCount = this.shortCount();
    if (shortCount < 4) {
      this.log.warn("shallow clone detected! git only has {} commits", shortCount);
    }
    return new DistanceCalculator(this.git).apply(Constants.HEAD);
  }

  @Override
  public GitStatus status() {
    return this.git.tryCommand(Git::status)
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
    return this.git.tryCommand(Git::remoteList)
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
    var remoteRef = Constants.R_REMOTES + remote + GIT_SEPARATOR + Constants.HEAD;
    return this.gitRepository()
      .mapTry(r -> r.findRef(remoteRef))
      .filter(Objects::nonNull)
      .filter(Ref::isSymbolic)
      .map(Ref::getLeaf)
      .map(Ref::getName)
      .recover(NoSuchElementException.class, e -> null)
      .onFailure(e -> this.log.error("failed to get HEAD branch", e))
      .getOrNull();
  }

  private static class RemoteImpl implements GitRemote {

    private final String name;
    private final @Nullable String headBranchRefName;

    RemoteImpl(String name, @Nullable String headBranchRefName) {
      this.name = name;
      this.headBranchRefName = headBranchRefName;
    }

    static GitRemote nullCheck(@Nullable String name, @Nullable String headBranchRefName) {
      return new RemoteImpl(Objects.requireNonNull(name), headBranchRefName);
    }

    @Override
    public @Nullable String headBranchRefName() {
      return this.headBranchRefName;
    }

    @Override
    public @NonNull String name() {
      return this.name;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
  }
}
