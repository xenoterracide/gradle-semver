// © Copyright 2024 Caleb Cushing
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
    return delegate.getUniqueShort().get();
  }

  @Override
  public @Nullable String tag() {
    return this.delegate.getTag().get();
  }

  @Override
  public int distance() {
    return this.delegate.getDistance().get();
  }

  @Override
  public GitStatus status() {
    return this.delegate.getStatus().get();
  }

  @Override
  public @Nullable String branch() {
    return this.delegate.getBranch().get();
  }

  @Override
  public @Nullable String commit() {
    return this.delegate.getCommit().get();
  }

  @Override
  public List<Remote> remotes() {
    return this.delegate.getRemotes().get();
  }
}
