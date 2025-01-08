// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Git Metadata interface to allow easy test stubbing with records.
 */
public interface GitMetadata {
  /**
   * Short version of a commit SHA.
   *
   * @return SHA. Length starts at 8 but may grow as repository does
   */
  @Nullable
  String uniqueShort();

  /**
   * Gets latest tag.
   *
   * @return the latest tag
   */
  @Nullable
  String tag();

  /**
   * Gets commit distance.
   *
   * @return the commit distance
   */
  long distance();

  /**
   * Gets status.
   *
   * @return the status
   */
  GitStatus status();

  /**
   * Gets the current branch.
   *
   * @return the current branch
   */
  @Nullable
  String branch();

  /**
   * Gets the current commit.
   *
   * @return the current commit
   */
  @Nullable
  String commit();

  /**
   * Gets the remotes.
   *
   * @return the configured remotes or an empty list
   */
  List<GitRemote> remotes();
}
