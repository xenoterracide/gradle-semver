// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

/**
 * Exception thrown when the HEAD branch is not set. Run
 * {@snippet lang = "sh":
 * git remote set-head <remote> --auto
 *}
 *
 * @see <a href="https://git-scm.com/docs/git-remote#Documentation/git-remote.txt-emset-headem">git remote set-head</a>
 */
public class HeadBranchNotAvailable extends RuntimeException {

  private static final long serialVersionUID = 1L;

  HeadBranchNotAvailable() {
    super("HEAD branch not available. Run `git remote set-head <remote> --auto`");
  }
}
