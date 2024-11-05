// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import static com.xenoterracide.gradle.git.internal.GradleTools.finalizeOnRead;

import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.gradle.api.provider.ValueSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GitValueSource<T, VSP extends GitValueSourceParameters> extends ValueSource<T, VSP> {
  static final Logger log = LoggerFactory.getLogger(GitValueSource.class);

  @Override
  default @Nullable T obtain() {
    try (var git = Git.open(finalizeOnRead(this.getParameters().getProjectDir()).get())) {
      return this.getValue(git);
    } catch (IOException e) {
      log.warn("Git had an exception", e);
    }
    return null;
  }

  @Nullable
  T getValue(Git git);
}
