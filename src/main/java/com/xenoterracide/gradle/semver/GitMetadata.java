// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import org.jspecify.annotations.Nullable;

public interface GitMetadata {
  @Nullable
  String uniqueShort();

  @Nullable
  String tag();

  int distance();
}
