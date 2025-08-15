// SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.Nullable;

/**
 * Delegates to {@link GitRemote} but uses Gradle's {@link Provider} for lazy evaluation.
 */
public class GitRemoteForGradle implements GitRemote {

  private final ProvidedFactory pf;
  private final String name;
  private final Provider<String> headBranch;
  private final Provider<String> headBranchRefName;

  GitRemoteForGradle(ProvidedFactory pf, GitRemote remote) {
    this.name = remote.name();
    this.headBranch = pf.providedString(remote::headBranch);
    this.headBranchRefName = pf.providedString(remote::headBranchRefName);
    this.pf = pf;
  }

  /**
   * Gets the name of the remote.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the head branch of the remote.
   *
   * @return the head branch
   */
  public Provider<String> getHeadBranch() {
    return this.headBranch.orElse(
        this.pf.providedString(() -> {
            Logging.getLogger(this.getClass()).warn(
              "Git remote {} has no HEAD branch; run `git remote set-head {} --auto`",
              this.name,
              this.name
            );
            return null;
          })
      );
  }

  Provider<String> getHeadBranchRefName() {
    return this.headBranchRefName;
  }

  @Override
  public @Nullable String headBranchRefName() {
    return this.headBranchRefName.getOrNull();
  }

  @Override
  public String name() {
    return this.name;
  }
}
