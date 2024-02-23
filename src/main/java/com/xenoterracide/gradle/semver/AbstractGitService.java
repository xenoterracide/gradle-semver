// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.AbstractGitService.Params;
import io.vavr.control.Try;
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

  private @Nullable Git git;
  private @Nullable Repository repository;

  @Inject
  @SuppressWarnings({ "this-escape", "InjectOnConstructorOfAbstractClass" })
  public AbstractGitService() {}

  Git lazyGit() throws IOException {
    if (this.git == null) {
      var builder = new FileRepositoryBuilder()
        .readEnvironment()
        .setMustExist(true)
        .findGitDir(this.getParameters().getProjectDirectory().get().getAsFile());
      this.repository = builder.build();
      this.git = new Git(this.repository);
    }

    return this.git;
  }

  public SemverExtension extension() {
    return new SemverExtension(() -> Try.of(this::lazyGit).onFailure(ExceptionTools::rethrow).get());
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
