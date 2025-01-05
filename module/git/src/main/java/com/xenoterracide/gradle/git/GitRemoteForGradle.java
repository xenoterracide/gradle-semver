// SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.Nullable;

public class GitRemoteForGradle {

  private final String name;
  private final Provider<@Nullable String> headBranch;

  public GitRemoteForGradle(ProvidedFactory pf, GitRemote remote) {
    this.name = remote.name();
    this.headBranch = pf.providedString(remote::headBranch);
  }

  String getName() {
    return this.name;
  }

  Provider<@Nullable String> getHeadBranch() {
    if (!this.headBranch.isPresent()) {
      Logging.getLogger(this.getClass()).warn(
        "Git remote {} has no HEAD branch; run `git remote set-head {} --auto`",
        this.name,
        this.name
      );
    }
    return this.headBranch;
  }
}
