// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Constants;
import org.jspecify.annotations.Nullable;

/**
 * Git Remote information.
 */
public interface GitRemote {
  /**
   * Gets the remote HEAD branch. This removes the {@code refs/heads/} prefix.
   *
   * @return HEAD branch
   */
  @Nullable
  default String headBranch() {
    return StringUtils.removeStart(this.headBranchRefName(), Constants.R_REMOTES + this.name() + "/");
  }

  @Nullable
  String headBranchRefName();

  /**
   * Gets the remote name; a common example is origin.
   *
   * @return remote name
   */
  String name();
}
