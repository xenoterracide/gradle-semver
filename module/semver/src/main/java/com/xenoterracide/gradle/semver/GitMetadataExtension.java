// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.GitMetadata;
import com.xenoterracide.gradle.semver.internal.ProvidedFactory;
import java.util.List;
import org.gradle.api.provider.Provider;

/**
 * Information about the state of the local git repository.
 */
public class GitMetadataExtension {

  private final Provider<String> uniqueShort;
  private final Provider<String> tag;
  private final Provider<Long> distance;
  private final Provider<GitStatus> status;
  private final Provider<String> branch;
  private final Provider<String> commit;
  private final Provider<List<GitRemote>> remotes;

  GitMetadataExtension(ProvidedFactory pf, GitMetadata gm) {
    this.uniqueShort = pf.providedString(gm::uniqueShort);
    this.tag = pf.providedString(gm::tag);
    this.distance = pf.providedLong(gm::distance);
    this.status = pf.provided(gm::status, GitStatus.class);
    this.branch = pf.providedString(gm::branch);
    this.commit = pf.providedString(gm::commit);
    this.remotes = pf.providedList(gm::remotes, GitRemote.class);
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
   * Gets status.
   *
   * @return the status
   */
  public Provider<GitStatus> getStatus() {
    return this.status;
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
  public Provider<List<GitRemote>> getRemotes() {
    return this.remotes;
  }
}
