// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.Ascii;

public enum GitStatus {
  CLEAN,
  DIRTY;

  @Override
  public String toString() {
    return Ascii.toLowerCase(this.name());
  }
}
