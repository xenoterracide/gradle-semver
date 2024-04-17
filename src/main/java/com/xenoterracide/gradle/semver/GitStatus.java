// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.CaseFormat;

/**
 * The enum Git status.
 */
public enum GitStatus {
  /**
   * Clean git status.
   */
  CLEAN,
  /**
   * Dirty git status.
   */
  DIRTY,
  /**
   * No Repository found.
   */
  NO_REPO;

  @Override
  public String toString() {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name());
  }
}
