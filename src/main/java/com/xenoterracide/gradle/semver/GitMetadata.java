// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

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
   * Delegate to generate Kotlin and Groovy getters.
   *
   * @return {@link #uniqueShort()}
   */
  default @Nullable String getUniqueShort() {
    return this.uniqueShort();
  }

  /**
   * Gets latest tag.
   *
   * @return the latest tag
   */
  @Nullable
  String tag();

  /**
   * Delegate to generate Kotlin and Groovy getters.
   *
   * @return {@link #tag()}
   */
  default @Nullable String getLatestTag() {
    return this.tag();
  }

  /**
   * Gets commit distance.
   *
   * @return the commit distance
   */
  int distance();

  /**
   * Delegate to generate Kotlin and Groovy getters.
   *
   * @return {@link #distance()}
   */
  default int getCommitDistance() {
    return this.distance();
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  GitStatus status();

  /**
   * Delegate to generate Kotlin and Groovy Getters.
   *
   * @return {@link #status()}
   */
  default GitStatus getStatus() {
    return this.status();
  }
}
