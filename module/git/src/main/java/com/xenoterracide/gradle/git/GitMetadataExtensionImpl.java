// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import javax.inject.Inject;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

class GitMetadataExtensionImpl implements GitMetadataExtension {

  private final ProviderFactory providerFactory;

  @Inject
  GitMetadataExtensionImpl(ProviderFactory providerFactory) {
    this.providerFactory = providerFactory;
  }

  @Override
  public Provider<String> getHeadBranch() {
    return this.providerFactory.of(HeadBranchValueSource.class, c -> {});
  }

  @Override
  public Provider<String> getSourceRemote() {
    return this.providerFactory.provider(() -> "origin");
  }

  @Override
  public Provider<String> getUniqueShort() {
    return this.providerFactory.provider(() -> "1234567");
  }

  @Override
  public Provider<String> getLatestTag() {
    return this.providerFactory.provider(() -> "v1.0.0");
  }

  @Override
  public Provider<Integer> getCommitDistance() {
    return this.providerFactory.provider(() -> 1);
  }

  @Override
  public Provider<Integer> getCommitDistanceOfAncestorInHeadBranch() {
    return this.providerFactory.provider(() -> 1);
  }

  @Override
  public Provider<GitStatus> getStatus() {
    return this.providerFactory.provider(() -> GitStatus.CLEAN);
  }
}
