// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.jspecify.annotations.Nullable;

/**
 * Git Remote information.
 */
public interface GitRemote {
  /**
   * Gets the remote HEAD branch.
   *
   * @return HEAD branch
   */
  @Nullable
  String headBranch();

  /**
   * Generates Kotlin/Groovy accessors for {@link #headBranch()}.
   *
   * @return HEAD branch
   */
  default @Nullable String getHeadBranch() {
    return this.headBranch();
  }

  /**
   * Gets the remote name; a common example is origin.
   *
   * @return remote name
   */
  String name();

  /**
   * Generates Kotlin/Groovy accessors for {@link #name()}.
   *
   * @return remote name
   */
  default String getName() {
    return this.name();
  }
}
