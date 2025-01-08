// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
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
   * Gets the remote name; a common example is origin.
   *
   * @return remote name
   */
  String name();
}
