// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import java.io.File;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;

public abstract class GitDirectoryValueSource implements ValueSource<File, GitDirectoryValueSource.Parameters> {

  @Override
  public @Nullable File obtain() {
    return new FileRepositoryBuilder()
      .readEnvironment(NoExecSystemReader.getCreateAndSet())
      .setMustExist(true)
      .findGitDir(this.getParameters().getProjectDirectory().getAsFile().getOrNull())
      .getGitDir();
  }

  public interface Parameters extends ValueSourceParameters {
    DirectoryProperty getProjectDirectory();
  }
}
