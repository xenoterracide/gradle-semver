// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import javax.inject.Inject;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

class GitMetadataExtensionImpl implements GitMetadataExtension {

  private final ProviderFactory project;

  @Inject
  GitMetadataExtensionImpl(ProviderFactory project) {
    this.project = project;
  }

  @Override
  public Provider<String> getHeadBranch() {
    return null;
  }

  @Override
  public Provider<String> getSourceRemote() {
    return null;
  }

  @Override
  public Provider<String> getUniqueShort() {
    return null;
  }

  @Override
  public Provider<String> getLatestTag() {
    return null;
  }

  @Override
  public Provider<Integer> getCommitDistance() {
    return null;
  }

  @Override
  public Provider<Integer> getCommitDistanceOfAncestorInHeadBranch() {
    return null;
  }

  @Override
  public Provider<GitStatus> getStatus() {
    return null;
  }
}
