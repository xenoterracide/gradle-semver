// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.xenoterracide.gradle.semver.SemverPlugin;
import java.io.File;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.gradle.testfixtures.ProjectBuilder;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class GitServiceTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  @NonNull
  File projectDir;

  @Test
  void noGit() {
    var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
    project.getPluginManager().apply(SemverPlugin.class);
    var gitSvc = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(GitService.class.getCanonicalName(), GitService.class)
      .get();
    assertThatExceptionOfType(RepositoryNotFoundException.class)
      .isThrownBy(gitSvc::get)
      .withMessage("repository not found: %s", projectDir);
  }
}
