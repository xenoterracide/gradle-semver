// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import java.io.File;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class NoGitDir extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private NoGitDir(File projectDirectory) {
    super("No git directory found in " + projectDirectory);
  }

  public static File requireGitDir(File projectDirectory) {
    var gitDir = new FileRepositoryBuilder()
      .readEnvironment()
      .setMustExist(true)
      .findGitDir(projectDirectory)
      .getGitDir();
    if (gitDir == null) {
      throw new NoGitDir(projectDirectory);
    }
    return gitDir;
  }
}
