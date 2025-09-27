// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ValueSource} that obtains a {@link Directory} representing a Git directory.
 */
public abstract class GitDirectoryValueSource implements ValueSource<Directory, GitDirectoryValueSource.Parameters> {

  private final Logger log = Logging.getLogger(this.getClass());

  @Override
  public @Nullable Directory obtain() {
    var proj = this.getParameters().getProjectDirectory();
    return proj
      .map(dir -> {
        var gitDir = new FileRepositoryBuilder()
          .readEnvironment(NoExecSystemReader.getCreateAndSet())
          .setMustExist(true)
          .findGitDir(dir.getAsFile())
          .getGitDir();
        this.log.debug("Located Git directory: {}", gitDir);
        return gitDir;
      })
      .flatMap(proj::fileValue)
      .getOrNull();
  }

  /**
   * Parameters for {@link GitDirectoryValueSource}.
   */
  public interface Parameters extends ValueSourceParameters {
    /**
     * Returns the project directory.
     *
     * @return the project directory
     */
    DirectoryProperty getProjectDirectory();
  }
}
