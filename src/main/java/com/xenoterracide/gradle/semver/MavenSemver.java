// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

class MavenSemver extends Semver {

  MavenSemver(@NotNull String version) {
    super(version);
  }

  @Override
  public String toString() {
    return super.getVersion().replace("+", "-");
  }
}
