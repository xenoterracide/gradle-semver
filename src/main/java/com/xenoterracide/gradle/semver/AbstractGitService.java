// SPDX-License-Identifier: Apache-2.0
// Copyright © 2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.AbstractGitService.GitVersionParameters;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

public abstract class AbstractGitService implements BuildService<GitVersionParameters> {

  private final FileRepositoryBuilder builder;

  @Inject
  AbstractGitService() {
    this.builder =
      new FileRepositoryBuilder()
        .readEnvironment(SystemReader.getInstance())
        .setMustExist(true)
        .findGitDir(this.getParameters().getProjectDirectory().get().getAsFile());
  }

  public @Nullable String gitVersion() {
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

  public interface GitVersionParameters extends BuildServiceParameters {
    DirectoryProperty getProjectDirectory();
  }
}
