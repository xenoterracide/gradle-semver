// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.provider.Provider;

public class GitExtension implements Provides<GitMetadata> {

  private final Provider<GitService> gitService;

  public GitExtension(Provider<GitService> gitService) {
    this.gitService = gitService;
  }

  @Override
  public Provider<GitMetadata> provider() {
    return this.gitService.map(GitService::provider).map(git -> new GitMetadataImpl(git::get));
  }
}
