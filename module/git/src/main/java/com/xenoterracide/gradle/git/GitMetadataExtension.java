// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.provider.Provider;

public interface GitMetadataExtension {
  Provider<String> getHeadBranch();

  Provider<String> getSourceRemote();

  Provider<String> getUniqueShort();

  Provider<String> getLatestTag();

  Provider<Integer> getCommitDistance();

  Provider<Integer> getCommitDistanceOfAncestorInHeadBranch();

  Provider<GitStatus> getStatus();
}
