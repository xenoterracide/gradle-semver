// SPDX-License-Identifier: Apache-2.0
// Copyright © 2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import java.io.File;
import java.io.IOException;
import javax.inject.Provider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class GitVersionProvider implements Provider<String> {

  private final File projectDirectory;

  GitVersionProvider(@NonNull File projectDirectory) {
    this.projectDirectory = projectDirectory;
  }

  @Override
  public @Nullable String get() {
    var builder = new FileRepositoryBuilder()
      .readEnvironment(SystemReader.getInstance())
      .setMustExist(true)
      .findGitDir(this.projectDirectory);

    try (var repo = builder.build()) {
      var git = new PorcelainGit(new Git(repo));
      return git.getSemver().getVersion();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
