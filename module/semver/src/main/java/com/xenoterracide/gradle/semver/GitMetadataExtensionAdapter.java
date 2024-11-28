// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.GitMetadata;
import java.util.List;
import org.jspecify.annotations.Nullable;

class GitMetadataExtensionAdapter implements GitMetadata {

  private final GitMetadataExtension delegate;

  GitMetadataExtensionAdapter(GitMetadataExtension delegate) {
    this.delegate = delegate;
  }

  @Override
  public @Nullable String uniqueShort() {
    return this.delegate.getUniqueShort().getOrNull();
  }

  @Override
  public @Nullable String tag() {
    return this.delegate.getTag().getOrNull();
  }

  @Override
  public long distance() {
    return this.delegate.getDistance().get();
  }

  @Override
  public GitStatus status() {
    return this.delegate.getStatus().get();
  }

  @Override
  public @Nullable String branch() {
    return this.delegate.getBranch().getOrNull();
  }

  @Override
  public @Nullable String commit() {
    return this.delegate.getCommit().getOrNull();
  }

  @Override
  public List<GitRemote> remotes() {
    return this.delegate.getRemotes().get();
  }
}
