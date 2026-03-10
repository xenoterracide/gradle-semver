// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import io.vavr.control.Try;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jspecify.annotations.Nullable;

/**
 * Build Service for Git. Primary goal is to allow for lazy initialization of the Git object and keeping it open for
 * later usage. This Service should not be considered a published API, and may change or be removed in future versions.
 */
public abstract class GitService implements BuildService<GitService.Params>, AutoCloseable {

  private @Nullable Git git;

  /**
   * Constructor for the Git Service.
   */
  @Inject
  @SuppressWarnings({ "this-escape", "InjectOnConstructorOfAbstractClass" })
  public GitService() {}

  /**
   * Provides a Provider for the Git object, lazily initialized and kept open for later usage. The Provider will be
   * memoized, so the Git object will only be initialized once per build. The Git object will be closed when the build
   * finishes, as this Service implements AutoCloseable.
   *
   * @return Provider of Git
   */
  public Provider<Git> getProvider() {
    return this.getParameters()
      .getGitDirectory()
      .map(Directory::getAsFile)
      .map(file -> this.git = this.git == null ? Try.ofCallable(() -> Git.open(file)).get() : this.git);
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
    DirectoryProperty getGitDirectory();
  }
}
