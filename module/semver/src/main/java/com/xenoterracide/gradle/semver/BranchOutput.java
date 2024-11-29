// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

/**
 * How you would like the branch name included in your semver.
 * <p>
 * In the members there are examples of the branch, and {@code master} is assumed to be the {@code HEAD branch}.
 * {@code master} is git's default branch, you can configure it to someting else, GitHub has made their default
 * {@code main}). All examples assume the same tag.
 */
public enum BranchOutput {
  /**
   * No branch output. {@code git describe} output is used as is. Every branch is treated as the HEAD Branch. On
   * branch.
   * <ul>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+g3aae11e}</li>
   *   <li>{@code foo/bar} - {@code 0.1.1-alpha.0.1+g3aae11e}</li>
   * </ul>
   */
  NONE,
  /**
   * The branch name is always included. THe distance is still calculated as {@code git describe} would.
   * <ul>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+bmaster.g3aae11e}</li>
   *   <li>{@code foo/bar} - {@code 0.1.1-alpha.0.1+bfoo-bar.g3aae11e}</li>
   * </ul>
   */
  ALWAYS,
  /**
   * The branch name is included if the branch is not the @{HEAD branch}. The distance is calculated from the
   * {@code git merge-base} between the {@code HEAD} and the {@code HEAD branch}.
   * <ul>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+g3aae11e}</li>
   *   <li>{@code foo/bar} - {@code 0.1.1-alpha.0.1+bfoo-bar.g3aae11e}</li>
   * </ul>
   */
  NON_HEAD_BRANCH_OR_FAIL,
  /**
   * This is the same as {@link #NON_HEAD_BRANCH_OR_FAIL} except that this will not fail if the head branch is not
   * set, instead falling back to {link #ALWAYS}.
   * <ul>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+g3aae11e}</li>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+bmaster.g3aae11e} if {@code HEAD branch} is not configured.</li>
   *   <li>{@code foo/bar} - {@code 0.1.1-alpha.0.1+bfoo-bar.g3aae11e}</li>
   * </ul>
   */
  NON_HEAD_BRANCH_FALLBACK_ALWAYS,
  /**
   * This is the same as {@link #NON_HEAD_BRANCH_OR_FAIL} except that this will not fail if the head branch is not
   * set, instead falling back to {@link #NONE}.
   * <ul>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+g3aae11e}</li>
   *   <li>{@code master}  - {@code 0.1.1-alpha.0.1+bmaster.g3aae11e} if {@code HEAD branch} is not configured.</li>
   *   <li>{@code foo/bar} - {@code 0.1.1-alpha.0.1+bfoo-bar.g3aae11e}</li>
   * </ul>
   */
  NON_HEAD_BRANCH_FALLBACK_NONE,
}
