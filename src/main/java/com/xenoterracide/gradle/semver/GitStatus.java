// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

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
