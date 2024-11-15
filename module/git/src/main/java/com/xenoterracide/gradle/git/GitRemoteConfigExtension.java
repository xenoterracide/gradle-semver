// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.provider.Property;

public interface GitRemoteConfigExtension {
  /**
   * The remote to use for the upstream source of truth. This remote is probably called "origin" by convention as most
   * git tutorials will call it that. Git itself has no restrictions on this. I sometimes use upstream as the name for
   * my source of truth, and origin for the name pointing to the fork that I can push to.
   *
   * @return the user set upstream remote
   * @implNote If only a single remote is defined, that will be the default.
   */
  Property<String> getUpstreamRemote();

  /**
   * The branch to use for the upstream source of truth. This branch is probably called "master" by convention as it is
   * the default branch for git. These days develop and main are also common names for this branch. This is set on the
   * remote but can be configured locally with {@code git remote set-head <remote> <branch>}.
   * <p>
   * {@see <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">git remote
   * set-head</a>}
   *
   * @return the explicitly configured head branch
   * @implNote Code will default to
   *   getting this branch from either the set-head or the remote if this is not set.
   */
  Property<String> getHeadBranch();
}
