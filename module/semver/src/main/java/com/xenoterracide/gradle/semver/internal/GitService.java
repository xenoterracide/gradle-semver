// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import com.xenoterracide.gradle.semver.internal.GitService.Params;
import io.vavr.control.Try;
import java.util.Objects;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jspecify.annotations.Nullable;

/**
 * Build Service for Git. Primary goal is to allow for lazy initialization of the Git object and keeping it open for
 * later usage. This Service should not be considered a published API, and may change or be removed in future versions.
 */
public abstract class GitService implements BuildService<Params>, AutoCloseable, TryGit {
  static {
    preventJGitFromCallingExecutables();
  }

  private @Nullable Git git;

  /**
   * Constructor for the Git Service.
   */
  @Inject
  @SuppressWarnings({ "this-escape", "InjectOnConstructorOfAbstractClass" })
  public GitService() {}

  static void preventJGitFromCallingExecutables() {
    SystemReader.setInstance(
      new SystemReader.Delegate(SystemReader.getInstance()) {
        @Override
        public String getenv(String variable) {
          if ("PATH".equals(variable)) {
            return "";
          } else {
            return super.getenv(variable);
          }
        }
      }
    );
  }

  @Override
  public Git get() {
    if (this.git == null) {
      var projectDir = this.getParameters().getProjectDirectory().get().getAsFile();
      var gitDir = new FileRepositoryBuilder().readEnvironment().setMustExist(false).findGitDir(projectDir).getGitDir();

      this.git = gitDir != null ? Try.ofCallable(() -> Git.open(gitDir)).get() : null;
    }

    return Objects.requireNonNull(this.git);
  }

  @Override
  public void close() {
    if (this.git != null) {
      this.git.close();
    }
  }

  /**
   * Parameters for the Git Service.
   */
  public interface Params extends BuildServiceParameters {
    /**
     * The project directory.
     *
     * @return The project directory.
     */
    DirectoryProperty getProjectDirectory();
  }
}
