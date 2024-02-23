// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

class MavenSemver extends Semver {

  MavenSemver(@NotNull String version) {
    super(version);
  }

  @Override
  public String getVersion() {
    return super.getVersion().replace("+", "-");
  }
}
