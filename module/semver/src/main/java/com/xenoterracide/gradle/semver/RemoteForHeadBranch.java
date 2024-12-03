// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

/**
 * Configure which remote to use for the HEAD branch. These only matter if more than one remote is configured.
 */
public enum RemoteForHeadBranch {
  /**
   * Use the configured remote, if missing use the remote named origin, or use the first remote we find.
   */
  CONFIGURED_ORIGIN_OR_FIRST,
  /**
   * Use the configured remote, if missing use the remote named origin, or throw an exception.
   */
  CONFIGURED_ORIGIN_OR_THROW,
}
