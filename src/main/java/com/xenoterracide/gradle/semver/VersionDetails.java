// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import org.jspecify.annotations.Nullable;

public interface VersionDetails {
  @Nullable
  String getBranchName();

  @Nullable
  String getGitHashFull();

  @Nullable
  String getGitHash();

  @Nullable
  String getLastTag();

  @Nullable
  String getDescribe();

  int getCommitDistance();

  boolean getIsCleanTag();
}
