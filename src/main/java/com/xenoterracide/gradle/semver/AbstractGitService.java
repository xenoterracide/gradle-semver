// SPDX-License-Identifier: Apache-2.0
// Copyright © 2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.AbstractGitService.Params;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jspecify.annotations.Nullable;

public abstract class AbstractGitService implements BuildService<Params>, AutoCloseable {

  private final FileRepositoryBuilder builder;
  private @Nullable Git git = null;
  private @Nullable Repository repository = null;

  @Inject
  @SuppressWarnings({ "this-escape", "InjectOnConstructorOfAbstractClass" })
  public AbstractGitService() {
    this.builder =
      new FileRepositoryBuilder()
        .readEnvironment()
        .setMustExist(true)
        .findGitDir(this.getParameters().getProjectDirectory().get().getAsFile());
  }

  PorcelainGit getPorcelainGit() throws IOException {
    this.repository = this.builder.build();
    this.git = new Git(this.repository);
    return new PorcelainGit(this.git);
  }

  @Override
  public void close() {
    if (this.git != null) this.git.close();
    if (this.repository != null) this.repository.close();
  }

  public interface Params extends BuildServiceParameters {
    DirectoryProperty getProjectDirectory();
  }
}
