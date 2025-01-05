// SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.Nullable;

public class GitRemoteForGradle {

  private final Logger log = Logging.getLogger(this.getClass());

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
    return this.headBranch;
  }
}
