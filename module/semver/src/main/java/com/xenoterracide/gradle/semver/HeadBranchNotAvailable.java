// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

/**
 * Exception thrown when the HEAD branch is not set. Run
 * {@snippet lang = "sh":
 * git remote set-head <remote> --auto
 *}
 *
 * @see <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">git remote set-head</a>
 */
public class HeadBranchNotAvailable extends RuntimeException {

  public HeadBranchNotAvailable() {
    super("HEAD branch not available. Run `git remote set-head <remote> --auto`");
  }
}
