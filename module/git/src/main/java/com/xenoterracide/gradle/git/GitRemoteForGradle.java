// SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.Incubating;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.Nullable;

public class GitRemoteForGradle {

  private final DistanceCalculator distanceCalculator;
  private final ProvidedFactory pf;
  private final String name;
  private final Provider<@Nullable String> headBranch;

  GitRemoteForGradle(ProvidedFactory pf, DistanceCalculator distanceCalculator, GitRemote remote) {
    this.distanceCalculator = distanceCalculator;
    this.name = remote.name();
    this.headBranch = pf.providedString(remote::headBranch);
    this.pf = pf;
  }

  public String getName() {
    return this.name;
  }

  public Provider<String> getHeadBranch() {
    return this.headBranch.orElse(
        pf.providedString(() -> {
          Logging.getLogger(this.getClass()).warn(
            "Git remote {} has no HEAD branch; run `git remote set-head {} --auto`",
            this.name,
            this.name
          );
          return null;
        })
      );
  }

  @Incubating
  public Provider<@Nullable Long> distanceFromTagInCommonAncestorFromHead() {
    return this.headBranch.map(this.distanceCalculator::apply);
  }
}
