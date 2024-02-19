// SPDX-License-Identifier: Apache-2.0
// Copyright © 2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Provider;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

class GitVersionProvider implements Provider<String> {

  private final File projectDirectory;

  GitVersionProvider(@NonNull File projectDirectory) {
    this.projectDirectory = projectDirectory;
  }

  @Override
  public @Nullable String get() {
    var builder = new FileRepositoryBuilder().findGitDir(this.projectDirectory);

    try (var repo = builder.build()) {
      var git = new PorcelainGit(new Git(repo));
      return Optional
        .ofNullable(git.describe())
        .map(v -> v.substring(1))
        .map(v -> v.contains("g") ? v + "-SNAPSHOT" : v)
        .orElse(null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
