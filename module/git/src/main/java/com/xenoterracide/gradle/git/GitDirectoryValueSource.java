// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import java.io.File;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ValueSource} that obtains a {@link File} representing a Git directory.
 */
public abstract class GitDirectoryValueSource implements ValueSource<File, GitDirectoryValueSource.Parameters> {

  @Override
  public @Nullable File obtain() {
    return new FileRepositoryBuilder()
      .readEnvironment(NoExecSystemReader.getCreateAndSet())
      .setMustExist(true)
      .findGitDir(this.getParameters().getProjectDirectory().getAsFile().getOrNull())
      .getGitDir();
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
