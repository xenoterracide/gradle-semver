// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jgit.lib.ObjectId;
import org.gradle.api.Incubating;
import org.gradle.api.provider.Provider;

/**
 * Extension for the {@link GitPlugin} mainly providing {@link Provider}'s for {@link GitMetadata}.
 */
public class GitExtension {

  private static final String HEAD_REF = "HEAD";

  private final Provider<GitMetadata> provider;
  private final Provider<org.eclipse.jgit.api.Git> git;
  private final Provider<String> uniqueShort;
  private final Provider<String> tag;
  private final Provider<Long> distance;
  private final Provider<GitStatus> status;
  private final Provider<String> branch;
  private final Provider<String> commit;
  private final Provider<List<GitRemoteForGradle>> remotes;

  @SuppressWarnings("NullAway")
  // false positive https://github.com/uber/NullAway/issues/1123
  GitExtension(Provider<GitService> gitService, ProvidedFactory pf) {
    this.git = gitService.flatMap(GitService::getProvider);
    this.provider = this.git.map(g -> new GitMetadataImpl(() -> g));
    this.branch = pf.providedString(this.provider.map(GitMetadata::branch));
    this.uniqueShort = pf.providedString(this.provider.map(GitMetadata::uniqueShort));
    this.tag = pf.providedString(this.provider.map(GitMetadata::tag));
    this.distance = pf.providedLong(this.provider.map(GitMetadata::distance).orElse(0L));
    this.status = pf.provided(this.provider.map(GitMetadata::status), GitStatus.class);
    this.commit = pf.providedString(this.provider.map(GitMetadata::commit));

    this.remotes = pf.providedList(
      this.provider.map(GitMetadata::remotes).map(remotes ->
        remotes
          .stream()
          .map(remote -> new GitRemoteForGradle(pf, remote))
          .collect(Collectors.toList())
      ),
      GitRemoteForGradle.class
    );
  }

  /**
   * Gets the GitMetadata provider.
   *
   * @return the GitMetadata provider
   */
  public Provider<GitMetadata> getProvider() {
    return this.provider;
  }

  /**
   * Gets the current branch.
   *
   * @return the current branch
   */
  public Provider<String> getBranch() {
    return this.branch;
  }

  /**
   * Short version of a commit SHA.
   *
   * @return SHA. Length starts at 8 but may grow as repository does
   */
  public Provider<String> getUniqueShort() {
    return this.uniqueShort;
  }

  /**
   * Gets latest tag.
   *
   * @return the latest tag
   */
  public Provider<String> getTag() {
    return this.tag;
  }

  /**
   * Gets commit distance.
   *
   * @return the commit distance
   */
  public Provider<Long> getDistance() {
    return this.distance;
  }

  /**
   * Gets dirty status.
   *
   * @return the status
   */
  public Provider<Boolean> getDirty() {
    return this.status.map(GitStatus.DIRTY::equals);
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public Provider<GitStatus> getStatus() {
    return this.status.orElse(GitStatus.NO_REPO);
  }

  /**
   * Gets the current commit.
   *
   * @return the current commit
   */
  public Provider<String> getCommit() {
    return this.commit;
  }

  /**
   * Gets the list of remotes.
   *
   * @return the list of remotes
   */
  @Incubating
  public Provider<List<GitRemoteForGradle>> getRemotes() {
    return this.remotes;
  }

  /**
   * will find the common ancestor between this branch and the given remote reference that is in your local git databse.
   * Essentially this is the distance from a tag from a {@code git merge base}.
   *
   * @param remote
   *   the remote
   * @return the distance
   */
  public Optional<Long> commonAncestorDistanceFor(GitRemoteForGradle remote) {
    return this.calculateDistanceFromMergeBase(remote);
  }

  /**
   * Overloaded method that accepts a {@link GitRemote} for convenience.
   *
   * @param remote
   *   the remote
   * @return the distance
   */
  public Optional<Long> commonAncestorDistanceFor(GitRemote remote) {
    return this.calculateDistanceFromMergeBase(remote);
  }

  private Optional<Long> calculateDistanceFromMergeBase(GitRemote remote) {
    var repository = this.git.get().getRepository();
    var oMergeBase = new MergeBaseFinder(repository).find(remote);
    return oMergeBase.flatMap(mergeBase -> this.distanceFromMergeBase(repository, mergeBase));
  }

  private Optional<Long> distanceFromMergeBase(org.eclipse.jgit.lib.Repository repository, ObjectId mergeBase) {
    try {
      return Optional.ofNullable(repository.resolve(HEAD_REF)).map(head ->
        new DistanceCalculator(this.git::get).distanceBetween(mergeBase, head)
      );
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
