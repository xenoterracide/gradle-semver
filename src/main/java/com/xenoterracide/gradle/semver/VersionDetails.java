// SPDX-License-Identifier: Apache-2.0
// Copyright © 2020-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import org.eclipse.jgit.annotations.Nullable;

public interface VersionDetails {
  @Nullable
  String getBranchName();

  @Nullable
  String getGitHashFull();

  @Nullable
  String getGitHash();

  @Nullable
  String getLastTag();

  int getCommitDistance();

  boolean getIsCleanTag();

  @Nullable
  String getVersion();
}
