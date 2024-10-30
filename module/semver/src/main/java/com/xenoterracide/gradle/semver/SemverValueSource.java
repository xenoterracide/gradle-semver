// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.AbstractGitService;
import javax.inject.Inject;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

public abstract class SemverValueSource implements ValueSource<Semver, ValueSourceParameters.None> {

  private final AbstractGitService gitService;

  @Inject
  protected SemverValueSource(AbstractGitService gitService) {
    this.gitService = gitService;
  }

  @Override
  public @Nullable Semver obtain() {
    return null;
  }
}
