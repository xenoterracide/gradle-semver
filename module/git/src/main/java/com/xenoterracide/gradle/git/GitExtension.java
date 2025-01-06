// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Incubating;
import org.gradle.api.provider.Provider;

public class GitExtension implements Provides<GitMetadata> {

  private final Provider<GitMetadata> gitMetadata;
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
    this.gitMetadata = gitService.map(GitService::provider).map(git -> new GitMetadataImpl(git::get));
    this.branch = pf.providedString(this.gitMetadata.map(GitMetadata::branch));
    this.uniqueShort = pf.providedString(this.gitMetadata.map(GitMetadata::uniqueShort));
    this.tag = pf.providedString(this.gitMetadata.map(GitMetadata::tag));
    this.distance = pf.providedLong(this.gitMetadata.map(GitMetadata::distance));
    this.status = pf.provided(this.gitMetadata.map(GitMetadata::status), GitStatus.class);
    this.commit = pf.providedString(this.gitMetadata.map(GitMetadata::commit));

    this.remotes = pf.providedList(
      this.gitMetadata.map(GitMetadata::remotes).map(remotes ->
          remotes
            .stream()
            .map(remote -> {
              var dc = new DistanceCalculator(gitService.flatMap(GitService::provider)::get);
              return new GitRemoteForGradle(pf, dc, remote);
            })
            .collect(Collectors.toList())
        ),
      GitRemoteForGradle.class
    );
  }

  @Override
  public Provider<GitMetadata> provider() {
    return this.gitMetadata;
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
}
